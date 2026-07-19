# Contributing to Next Move

Focused bug fixes and small, reviewable improvements are welcome.

## Before opening a pull request

1. Open or reference an issue when the behavior or scope is not obvious.
2. Keep the change limited to one concern.
3. Preserve the plugin's read-only behavior and disabled-by-default public
   lookup boundary.
4. Follow RuneLite's Plugin Hub conventions and the repository guidance in
   `AGENTS.md`.
5. Run the complete Java 11 verification:

   ```sh
   JAVA_HOME=/path/to/jdk-11 ./gradlew clean check --no-daemon
   ```

6. Complete the relevant checks in `docs/manual-qa.md` for visible behavior.

Do not include account credentials, private RuneLite data, generated build
artifacts, or unrelated formatting changes. Gameplay automation and prohibited
combat helpers are outside this project's scope.

Security concerns should follow `SECURITY.md`, not a public issue.
