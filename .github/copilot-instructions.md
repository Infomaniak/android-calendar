# Copilot Coding Agent Onboarding — android-calendar

> **Read `AGENTS.md` and `app/AGENTS.md` first** for architecture and conventions. This file covers PR review instructions.


## PR Review Instructions

- Ensure strings are localized via `strings.xml` resources. App strings live under `app/src/main/res/values*/*`; any strings used
  by `CalendarComponents/*` must go in `CalendarComponents/Resources/src/main/res/values*/*` (and translations in the
  corresponding `values-<lang>` folders). Do not add strings to other `CalendarComponents/*` modules.
- Make sure fdroid is not forgotten when adding new behavior.
- When adding/removing a runtime dependency, update `LICENSES.md` at the repo root.
- If a module contains strings, make sure it is covered in `workflows/translations-validation.yml` and validated there. If it is
  missing, add:- If a module contains strings, make sure the module is correctly validated inside
  the [translations-validation.yml](workflows/translations-validation.yml) CI file. If missing, you can suggest to add the
  following code by adapting it to the new module's actual name:
```yml
      - name: Run Ink validation for <module name>
        run: |
          source ink_utils/venv/bin/activate
          python ink_utils/main.py loco --module <relative module path> --check --verbose
```

## Conventional Comments

> Use [Conventional Comments](https://conventionalcomments.org/) to format review feedback exactly like this:

```
**<label> [decorations]:** <subject>

[optional discussion]
```

The subject should not be more than one short line/sentence. If more information is required to understand the comment, put it into the discussion part.

Use these labels:

- `issue`: Issues highlight specific problems with the subject under review.
- `suggestion`: Suggestions propose improvements to the current subject. It’s important to be explicit and clear on what is being suggested and why it is an improvement.
- `todo`: TODOs are small, trivial, but necessary changes.
- `typo`: Typo comments are like todo comments, where the main issue is a misspelling.
- `quibble`: Use that one instead of `nitpick` for trivial preference- or style-based requests. These should be non-blocking by nature.
- `polish`: Polish comments are like a suggestion, where there is nothing necessarily wrong with the relevant content, there are just some ways to immediately improve the quality.
- `note`: Notes are always non-blocking and simply highlight something the reader should take note of.

You may use decorations after the label, but only if it really improves the value:

- `(blocking)` A comment with this decoration should prevent the subject under review from being accepted, until it is resolved.
- `(non-blocking)` A comment with this decoration should not prevent the subject under review from being accepted.
- `(if-minor)` This decoration gives some freedom to the author that they should resolve the comment only if the changes end up being minor or trivial.

