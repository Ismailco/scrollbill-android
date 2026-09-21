# Google Play Data Safety Preparation (Internal)

This is an internal preparation note for the current ScrollBill 0.1.1 implementation. It is not a completed Google Play declaration; the final questionnaire and policy interpretation require human review before submission.

## Current implementation facts

- UsageStats history is processed locally on the device.
- There is no ScrollBill server, network client, Internet permission, account, cloud sync, analytics, advertising, or crash-reporting SDK.
- No usage history, report database, or installed-app inventory is persisted.
- A receipt PNG is temporarily stored in private app cache for explicit Android Sharesheet sharing.
- Sharing is user initiated. The receiving application may process the image under its own policies and is outside ScrollBill's control.
- The app does not intentionally collect location, contacts, financial data, or health data.

## Items for human review

- Determine how Google Play's questionnaire should classify Android Usage Access and locally processed app-usage information, even though ScrollBill does not transmit it to a server.
- Confirm whether the temporary receipt and explicit transfer to a user-selected third-party app require any additional disclosure wording in the final Play listing or Data Safety form.
- Complete the final declarations against the exact release build and current Google Play policy text.
