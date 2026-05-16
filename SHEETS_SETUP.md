# InvoiceBot - Google Sheets Setup Guide

## Creating Your Google Sheet

Here's an example structure for the Google Sheet:

### Sheet 1: "Invoices"

| Invoice ID | Client Name | Amount | Due Date | Status | Description |
|------------|-------------|--------|----------|--------|-------------|
| INV-001 | Acme Corporation | $1,500.00 | 2024-02-15 | Paid | Web Development Services |
| INV-002 | Tech Solutions LLC | $2,750.50 | 2024-02-20 | Pending | Consulting Services |
| INV-003 | Global Industries | $3,200.00 | 2024-02-25 | Overdue | System Maintenance |

When you run `!invoice` in Discord, the bot will fetch this data and display each row as a formatted message.

### Sheet 2: "Payments"

| Payment ID | From Client | Amount | Date | Reference |
|------------|-------------|--------|------|-----------|
| PAY-001 | Acme Corporation | $1,500.00 | 2024-02-10 | Bank Transfer |
| PAY-002 | Tech Solutions LLC | $2,750.50 | 2024-02-18 | Check |

Run `!data Payments` to fetch this sheet.

### Sheet 3: "Clients"

| Client ID | Company Name | Email | Phone | Status |
|-----------|-------------|-------|-------|--------|
| CLI-001 | Acme Corporation | contact@acme.com | (555) 123-4567 | Active |
| CLI-002 | Tech Solutions LLC | info@techsol.com | (555) 987-6543 | Active |

## How to Set Up the Sheet

1. **Create a new Google Sheet:**
   - Go to https://sheets.google.com
   - Click "Create new spreadsheet"
   - Name it "InvoiceBot" or your preferred name

2. **Add headers and data:**
   - Click on the first sheet tab at the bottom (default "Sheet1")
   - Rename it to "Invoices" (or your desired name)
   - Add headers in row 1
   - Add data starting from row 2

3. **Get the Sheet ID:**
   - The URL looks like: `https://docs.google.com/spreadsheets/d/ABC123DEF456.../edit`
   - Copy the ID (ABC123DEF456...) - this is your SPREADSHEET_ID

4. **Share with the service account:**
   - Click "Share" button
   - Paste the service account email from credentials.json
   - Give it "Viewer" access
   - Send the invitation

## Sheet Naming and Querying

- Sheet names are case-sensitive
- Use single quotes if the sheet name has spaces: `'Invoice Data'`
- The bot can read multiple sheets from the same spreadsheet

Examples:
```
!data Invoices      # Reads the "Invoices" sheet
!data Payments      # Reads the "Payments" sheet
!data Clients       # Reads the "Clients" sheet
```

## Tips

- Keep data organized with clear headers
- Use consistent formatting for dates and amounts
- Don't leave blank rows in the middle of your data
- The bot reads up to column Z - organize data accordingly
- Add new rows to update the data the bot returns
