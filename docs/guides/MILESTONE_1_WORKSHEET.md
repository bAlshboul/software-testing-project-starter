# Milestone 1 Worksheet

## Set up the team repository

Create a team-owned copy of the instructor's starter. If the team already has a repository for this project, use it for both milestones. Invite the other two students as collaborators. Each student pushes their own work from their own account. Keep the test and implementation commits when merging branches so the report can refer to the original commits.

The Java classes and Mockito setup are a suggested starting structure. Review the signatures together before developing the behaviors. `BookingManager.book` currently throws `UnsupportedOperationException`. `BookingManagerTest` contains the fixture. The starter's `EvenNumberCheckerTest` is a separate example.

## Review the proposed contracts

- `BookingRequest` holds the submitted fields. `BookingManager` is the entry point that should reject invalid requests before using collaborators.
- `BookingResult` proposes one status for each of the four behaviors. Decide whether and how to enforce the transaction ID invariant in this type.
- `IPaymentGateway` uses `Optional.empty()` for a failed payment and specifies a nonblank transaction ID on success. Discuss what the manager should do if an implementation violates this contract.
- Booking and payment amounts use `long` so the total can represent five tickets at `Integer.MAX_VALUE` cents each. Check where Java must widen an operand to get a `long` product.

## Develop and verify the behaviors

| Suggested owner and order | Behavior | Questions to answer in your tests |
| --- | --- | --- |
| Student 1 | M1-US1 successful booking | Which quantity and price make the amount calculation visible? Which fields in the saved booking and confirmation establish that the right customer, event, amount and transaction were used? Verify the sold-out check, payment, save and confirmation once each. |
| Student 2 | M1-US2 invalid request | Which representative values expose missing, empty and whitespace-only IDs, quantities outside 1–5, and nonpositive prices? Use a parameterized test that varies one field at a time. How will you verify the absence of interactions with all three mocks? |
| Student 3 | M1-US3 sold-out event | How will you stub availability and verify one availability check, zero payments, zero saves and zero confirmations? Which result identifies this path? |
| Student 3 | M1-US4 failed payment | How will you stub an available event and payment failure? Which assertions verify one availability check, one payment attempt at the expected amount, and zero saves and confirmations? |

For each added test, derive the expected result independently from the assignment. Use explicit Mockito stubs where a collaborator's response controls the path. In the sold-out and payment-failure tests, verify the forbidden save separately since the repository has already handled the availability check.

## Record two actual TDD cycles

M1-US1 and M1-US2 can provide the required cycles. For each, the student implementing the behavior works in this order.

1. Add and run the test before implementing the behavior. Check that it compiles and that the failure indicates the missing behavior. Commit the failing test and note the failure it exposed.
2. Make the smallest change that passes the test. Run `mvn clean test` and commit the working implementation.
3. Improve a real issue in the production or test code while preserving behavior. Rerun `mvn clean test` and commit the refactor. Record the three hashes and explain the change in the report.

## Prepare the submission

1. Use `docs/templates/REPORT_TEMPLATE.md` for the Milestone 1 report. Explain the booking workflow and constructor injection using your implementation. For the mocking discussion, identify one plausible collaborator defect a passing mock-based test would miss and explain why.
2. Complete `docs/templates/TEAM_CONTRIBUTION_MATRIX.md` from the team's actual files and commits. Include the matrix in the report. The recommended body length is 3–5 pages.
3. Record any retained material AI help using `docs/templates/AI_USE_LOG.md`. The draft entry for this scaffold is **23 Nov 2026 | ChatGPT Codex | Suggested Java booking contracts, constructor injection and the Mockito fixture | `src/main/java/edu/course/booking/` and `BookingManagerTest.java`**. If you retain it, complete the entry with what you accepted or changed and how you independently checked it. Include the log in the report appendix or as a separate `ai-use-log.pdf`.
4. Commit the report source with the booking code and tests. On the final commit, run `mvn clean test`, record the actual results, and use `git rev-parse HEAD` to get the hash. Put the full hash in the external manifest. If you add the hash to the PDF cover, do it when exporting the Moodle copy so the hash still identifies the committed report source.
5. Export `report.pdf` and fill out `submission-manifest.txt`, including the AI Use Declaration. Run `bash scripts/create-submission.sh 1 /path/to/report.pdf /path/to/submission-manifest.txt` from the repository root to create the three-file ZIP. Add `ai-use-log.pdf` if submitting the log separately. Open the ZIP, restore `repository.bundle` into a fresh clone, check out the manifest hash and run `mvn clean test` there before submitting the ZIP and team repository URL through Moodle.
