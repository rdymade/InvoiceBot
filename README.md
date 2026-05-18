# InvoiceBot - Discord Bot with Google Sheets Integration for Alliance Auth

A Discord bot built with Groovy/Java that fetches data from Google Sheets and responds to commands with formatted messages.

The bot relies on two important changes made to the alliance auth discord bot and invoices plugins, see the following repositories for the changes:
https://github.com/rdymade/allianceauth-discordbot
https://github.com/rdymade/allianceauth-invoice-manager

## Features

- 🤖 Discord command handling with custom prefix
- 📊 Google Sheets integration for data fetching
- 📄 Automatic formatting of spreadsheet data into Discord messages
- ⌚ Automated sending of invoices on the 1st of each month
- 🔐 Secure authentication using Google Service Accounts
- ⚙️ Environment-based configuration

## Prerequisites

- Java 21+
- Gradle 9.5.0+
- Discord Bot Token
- Google Cloud Project with Sheets API enabled
- Google Service Account credentials (JSON file)

## Setup Instructions

### 1. Create a Discord Bot

1. Go to [Discord Developer Portal](https://discord.com/developers/applications)
2. Click "New Application" and name it "InvoiceBot"
3. Go to "Bot" section and click "Add Bot"
4. Copy the bot token (you'll need this for `DISCORD_BOT_TOKEN`)
5. Under "Intents", enable:
   - Server Members Intent
   - Message Content Intent
6. Go to "OAuth2" → "URL Generator"
   - Select scopes: `bot`
   - Select permissions: `Send Messages`, `Read Message History`, `Manage Messages`
   - Copy the generated URL and use it to invite the bot to your server

### 2. Set Up Google Cloud Project

1. Go to [Google Cloud Console](https://console.cloud.google.com)
2. Create a new project
3. Enable the Google Sheets API:
   - Search for "Google Sheets API" and enable it
4. Create a Service Account:
   - Go to "Service Accounts" under "IAM & Admin"
   - Create a new service account
   - Add a JSON key to the service account
   - Download the JSON file and save it as `credentials.json` in your project root

### 3. Share Google Sheet with Service Account

1. Open your Google Sheet
2. Get the Sheet ID from the URL: `https://docs.google.com/spreadsheets/d/{SHEET_ID}/edit`
3. In the `credentials.json` file, find the `client_email` value
4. Share the Google Sheet with that email address (with Viewer permissions)

### 4. Set Environment Variables

Create a `.env` file or export these environment variables:

```bash
export DISCORD_BOT_TOKEN="your-bot-token-here"
export SPREADSHEET_ID="your-sheet-id-here"
export GOOGLE_CREDENTIALS_PATH="./app/credentials.json"
export COMMAND_PREFIX="!"
export ALLOWED_USER_IDS="123456789012345678,987654321098765432"
```

On Linux/Mac, you can load these from a file:
```bash
source .env
```

### 5. Build and Run

```bash
# Build the project
./gradlew build

# Run the bot
./gradlew run
```

The bot will start and print an invite URL to the console. Once connected, it will listen for commands in Discord.

## Docker Deployment

This project includes a `Dockerfile` and a GitHub Actions workflow that builds and pushes a Docker image automatically.

### Local Docker build

```bash
docker build -t invoicebot .

docker run \
  --env DISCORD_BOT_TOKEN="$DISCORD_BOT_TOKEN" \
  --env SPREADSHEET_ID="$SPREADSHEET_ID" \
  --env GOOGLE_CREDENTIALS_PATH="/app/credentials.json" \
  --env COMMAND_PREFIX="!" \
  --env ALLOWED_USER_IDS="$ALLOWED_USER_IDS" \
  -v /path/to/credentials.json:/app/credentials.json:ro \
  invoicebot
```

### Using Docker Compose (recommended)

The easiest way to run the bot with Docker is using `docker-compose.yaml`:

1. Copy `.env.example` to `.env` and fill in your values:

```bash
cp .env.example .env
```

2. Edit `.env` with your configuration:

```bash
DISCORD_BOT_TOKEN="your-bot-token"
SPREADSHEET_ID="your-spreadsheet-id"
COMMAND_PREFIX="!"
ALLOWED_USER_IDS="123456789012345678,987654321098765432"
```

3. Ensure `credentials.json` is in the project root (same directory as `docker-compose.yaml`).

4. Run the bot:

```bash
docker-compose up -d
```

View logs:

```bash
docker-compose logs -f invoicebot
```

Stop the bot:

```bash
docker-compose down
```

### GitHub automated Docker build

1. Create a GitHub repository and push this code.
2. The workflow at `.github/workflows/docker-image.yml` runs on pushes to `main` or `master`.
3. It builds the app using Gradle and publishes the image to GitHub Container Registry.

The image is published as:

```
ghcr.io/${{ github.repository_owner }}/invoicebot:latest
```

If you want to deploy the bot to your server, pull the image and run it with the required environment variables.

## Available Commands

- `!invoice` - Fetch and display all invoices from the "Invoices" sheet
- `!data <sheet_name>` - Fetch data from a specific sheet
  - Example: `!data Invoices`
- `!help` - Show available commands

## Google Sheets Structure

### Invoices Sheet

Create a sheet named "Farm_list" with columns like:

| Main | Director | Paying Character | System | Comment | Status | Type | Amount |
|------|----------|------------------|--------|---------|--------|------|--------|
| Char1 | Holding Char | Knecht1 | Jita | Inhabitant A,B,C | x | HS | 1000000000 |
| Char2 | Holding Char2 | Knecht2 | Amarr | Inhabitant D,F,G |  | HS | 1000000000 |

The bot will fetch these rows and format them into Discord messages, only if the status is not empty.

## Project Structure

```
app/
├── src/main/groovy/fun/lupus/
│   ├── App.groovy                    # Main entry point
│   ├── DiscordBot.groovy             # Bot initialization
│   ├── DiscordCommandListener.groovy  # Command handler
│   ├── GoogleSheetsService.groovy    # Sheets integration
│   └── BotConfig.groovy              # Configuration loader
└── build.gradle                      # Dependencies
```

## Configuration

The bot loads configuration from environment variables:

| Variable | Description | Default |
|----------|-------------|---------|
| `DISCORD_BOT_TOKEN` | Discord bot token (required) | - |
| `SPREADSHEET_ID` | Google Sheet ID (required) | - |
| `GOOGLE_CREDENTIALS_PATH` | Path to credentials.json | `./credentials.json` |
| `COMMAND_PREFIX` | Command prefix character | `!` |
| `ALLOWED_USER_IDS` | Comma-separated list of Discord user IDs allowed to issue commands | - |
| `INVOICE_SHEET_NAME` | Name of the sheet to fetch invoice data from | - |
| `INVOICE_RANGE` | Cell range for invoice data (e.g., 'E2:L') | - |
| `DEFAULT_CHANNEL_ID` | Default channel for automated invoicing | - |

## Error Handling

- Invalid credentials will cause the bot to fail on startup
- Missing sheets will show an error message in Discord
- Long responses are automatically split to stay within Discord's 2000 character limit

## Extending the Bot

### Adding New Commands

Edit `DiscordCommandListener.groovy` and add a new case to the switch statement:

```groovy
case 'mycommand':
    handleMyCommand(event, args)
    break

private void handleMyCommand(MessageReceivedEvent event, String[] args) {
    // Your command logic here
    event.channel.sendMessage("Response").queue()
}
```

### Fetching Specific Ranges

Use `sheetsService.fetchData()` for specific ranges:

```groovy
def data = sheetsService.fetchData("Invoices!A1:D10")
```

## Troubleshooting

**Bot doesn't respond:**
- Verify the bot has message permissions in the channel
- Check that `DISCORD_BOT_TOKEN` is set correctly
- Ensure Message Content Intent is enabled in Discord Developer Portal

**Google Sheets errors:**
- Verify the sheet is shared with the service account email
- Check that credentials.json is in the correct location
- Ensure the sheet tab name matches exactly (case-sensitive)

**Build errors:**
- Run `./gradlew clean` and then `./gradlew build`
- Verify Java 21 is installed: `java --version`

## License

This project is open source and available under the MIT License.
