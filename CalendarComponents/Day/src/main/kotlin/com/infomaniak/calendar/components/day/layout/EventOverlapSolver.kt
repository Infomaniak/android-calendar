/*
 * Infomaniak Calendar - Android
 * Copyright (C) 2026 Infomaniak Network SA
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.infomaniak.calendar.components.day.layout

import androidx.compose.runtime.Immutable
import com.infomaniak.calendar.components.day.model.MINUTES_PER_DAY
import com.infomaniak.calendar.components.day.model.TimedEvent
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round

/**
 * Where and how big a card is drawn, in pixels relative to the events area.
 *
 * [visibleHeight] is how much of the card is still uncovered before the first card drawn over it
 * begins, so it can trim its content instead of writing underneath its neighbour. It equals
 * [height] when nothing covers the card.
 */
@Immutable
data class EventPlacement(
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val visibleHeight: Float,
    val drawOrder: Float,
)

/**
 * Arranges concurrent events, following the same rules as the Eventually layout the iOS calendar
 * uses, so a day looks the same on both platforms.
 *
 * Top and height come from the event's hours and are never negotiable. Left and width are: an event
 * takes the widest area it can get.
 *
 * Events whose title areas would collide, meaning their starts are less than
 * [EventLayoutConfig.titleHeight] apart, form a group and share the width side by side. An event
 * starting further down opens a new group and settles into the space still free, split by the left
 * edges of everything already placed. It therefore lands over an earlier event rather than forcing
 * the whole day into narrower columns.
 *
 * An event that no longer fits anywhere collapses to nothing instead of piling up on its neighbours.
 *
 * Placements come back in the order of the receiver.
 */
internal fun List<TimedEvent>.resolveOverlaps(
    layoutWidth: Float,
    pixelsPerMinute: Float,
    config: EventLayoutConfig,
): List<EventPlacement> {
    if (isEmpty()) return emptyList()

    val timelineHeight = MINUTES_PER_DAY * pixelsPerMinute
    val order = indices.sortedWith(compareBy({ this[it].startMinuteOfDay }, { -this[it].durationMinutes }))

    val frames = ArrayList<EventFrame>(size)
    var groupStart = 0

    order.forEachIndexed { position, index ->
        val frame = this[index].toFullWidthFrame(layoutWidth, pixelsPerMinute, timelineHeight, config)
        if (position != 0 && frame.y - frames[groupStart].y > config.titleHeight) {
            alignGroup(frames, groupStart, position, layoutWidth, config)
            groupStart = position
        }

        frames.add(frame)
    }
    alignGroup(frames, groupStart, frames.size, layoutWidth, config)

    val drawnPlacements = frames.mapIndexed { position, frame -> frame.toPlacement(position.toFloat(), config) }
    val visibleHeights = drawnPlacements.visibleHeights()

    val placements = arrayOfNulls<EventPlacement>(size)
    order.forEachIndexed { position, index ->
        placements[index] = drawnPlacements[position].copy(visibleHeight = visibleHeights[position])
    }

    return runCatching { placements.requireNoNulls().asList() }.onFailure {
        //TODO[sentry] Add log error when it happens
    }.getOrNull() ?: placements.filterNotNull()
}

/**
 * For every card, the distance down to the first card drawn over it, or its full height when it
 * stays uncovered. Cards are already in draw order, so only the ones after it can cover it.
 */
private fun List<EventPlacement>.visibleHeights(): List<Float> = mapIndexed { position, covered ->
    if (covered.isCollapsed) return@mapIndexed covered.height

    var visibleHeight = covered.height
    for (upperPosition in position + 1 until size) {
        val covering = this[upperPosition]
        if (covering.isCollapsed || !covered.overlaps(covering)) continue

        val uncoveredHeight = covering.y - covered.y
        if (uncoveredHeight > 0f) visibleHeight = min(visibleHeight, uncoveredHeight)
    }

    visibleHeight
}

private val EventPlacement.isCollapsed: Boolean get() = width <= 0f || height <= 0f

private fun EventPlacement.overlaps(other: EventPlacement): Boolean {
    val sharedWidth = min(x + width, other.x + other.width) - max(x, other.x)
    val sharedHeight = min(y + height, other.y + other.height) - max(y, other.y)

    return sharedWidth > 1f && sharedHeight > 1f
}

private fun TimedEvent.toFullWidthFrame(
    layoutWidth: Float,
    pixelsPerMinute: Float,
    timelineHeight: Float,
    config: EventLayoutConfig,
): EventFrame {
    val y = startMinuteOfDay * pixelsPerMinute
    // The gap to the next event comes out of the height first, so the floor is what is really drawn.
    val height = ((durationMinutes * pixelsPerMinute).coerceAtMost(timelineHeight - y) - config.verticalSpacing)
        .coerceAtLeast(config.minEventHeight)

    return EventFrame(x = 0f, y = y, width = layoutWidth, height = height)
}

/**
 * Spreads one group of events over the horizontal space the already aligned events left free.
 * Events before [from] are considered settled and are never moved.
 */
private fun alignGroup(frames: List<EventFrame>, from: Int, until: Int, layoutWidth: Float, config: EventLayoutConfig) {
    if (from >= until) return

    val group = frames.subList(from, until)
    val containers = buildContainers(frames, from, group.minOf { it.y }, group.maxOf { it.maxY }, layoutWidth, config)

    distributeAmongContainers(group, containers).forEach { (containerIndex, groupIndices) ->
        val container = containers[containerIndex]
        val fairShare = (container.width - container.padding) / groupIndices.size
        // The frame still carries the trailing spacing toPlacement() strips, so the floor includes
        // it to keep the drawn card at least minEventWidth wide.
        val eventWidth = fairShare.coerceAtLeast(config.minEventWidth + config.horizontalSpacing)
        // An even split fills the container exactly, so crossing maxX can only be float rounding
        // noise: events can genuinely run out of room only once the floor overrides the split.
        val isFloored = eventWidth > fairShare

        groupIndices.forEachIndexed { slot, groupIndex ->
            val x = container.x + eventWidth * slot + container.padding
            val frame = group[groupIndex]

            if (isFloored && x + eventWidth > container.maxX) frame.collapse() else frame.moveTo(x, eventWidth)
        }
    }
}

/**
 * Cuts the full width into the slots a group may use, one per gap between the left edges of the
 * events already placed across it. A left edge only counts below its own title.
 */
private fun buildContainers(
    frames: List<EventFrame>,
    alignedCount: Int,
    groupTop: Float,
    groupBottom: Float,
    layoutWidth: Float,
    config: EventLayoutConfig,
): List<FrameContainer> {
    val edges = mutableListOf(0f)

    for (index in 0 until alignedCount) {
        val placed = frames[index]
        val bodyTop = placed.y + config.titleHeight

        val crossesGroup = placed.width > 0f && bodyTop < placed.maxY && bodyTop < groupBottom && groupTop < placed.maxY
        if (crossesGroup && placed.x < layoutWidth) edges.add(placed.x)
    }

    edges.add(layoutWidth)
    edges.sort()

    val containers = mutableListOf<FrameContainer>()
    for (index in 0 until edges.lastIndex) {
        if (edges[index] + config.horizontalPadding >= edges[index + 1]) continue

        val padding = if (containers.isEmpty() && index == 0) 0f else config.horizontalPadding
        containers.add(FrameContainer(edges[index], edges[index + 1] - edges[index], padding))
    }

    return containers.ifEmpty { listOf(FrameContainer(x = 0f, width = 0f, padding = 0f)) }
}

/** Every event joins the container where it would still get the widest share. */
private fun distributeAmongContainers(
    group: List<EventFrame>,
    containers: List<FrameContainer>,
): Map<Int, List<Int>> {
    val eventsByContainer = mutableMapOf<Int, MutableList<Int>>()

    group.indices.forEach { groupIndex ->
        var widestShare = 0f
        var chosenContainer = 0

        containers.forEachIndexed { containerIndex, container ->
            val occupants = eventsByContainer[containerIndex]?.size ?: 0
            val share = ((container.width - container.padding) / (occupants + 1)).roundedToHundredths()

            if (widestShare < share) {
                widestShare = share
                chosenContainer = containerIndex
            }
        }

        eventsByContainer.getOrPut(chosenContainer) { mutableListOf() }.add(groupIndex)
    }

    return eventsByContainer
}

private fun EventFrame.toPlacement(drawOrder: Float, config: EventLayoutConfig): EventPlacement {
    val height = height.coerceAtLeast(0f)

    return EventPlacement(
        x = x,
        y = y,
        width = (width - config.horizontalSpacing).coerceAtLeast(0f),
        height = height,
        visibleHeight = height,
        drawOrder = drawOrder,
    )
}

private fun Float.roundedToHundredths(): Float = round(this * 100f) / 100f

private class EventFrame(var x: Float, var y: Float, var width: Float, var height: Float) {
    val maxY get() = y + height

    fun moveTo(x: Float, width: Float) {
        this.x = x
        this.width = width
    }

    fun collapse() {
        x = 0f
        y = 0f
        width = 0f
        height = 0f
    }
}

private class FrameContainer(val x: Float, val width: Float, val padding: Float) {
    val maxX get() = x + width
}
