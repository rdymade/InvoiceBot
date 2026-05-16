package fun.lupus

/**
 * Main entry point for the InvoiceBot Discord bot
 */
class App {
    static void main(String[] args) {
        try {
            println("InvoiceBot starting up...")
            println("=" * 50)

            // Load configuration from environment variables
            String discordToken = BotConfig.discordToken
            String credentialsPath = BotConfig.googleSheetsCredentials
            String spreadsheetId = BotConfig.spreadsheetId
            String commandPrefix = BotConfig.commandPrefix
            Set<String> allowedUserIds = BotConfig.allowedUserIds
            String invoiceSheetName = BotConfig.invoiceSheetName
            String invoiceRange = BotConfig.invoiceRange

            println("Configuration loaded:")
            println("  Discord Token: ${discordToken ? 'Set' : 'NOT SET'}")
            println("  Google Credentials: $credentialsPath")
            println("  Spreadsheet ID: ${spreadsheetId ? 'Set' : 'NOT SET'}")
            println("  Command Prefix: $commandPrefix")
            println("  Allowed users: ${allowedUserIds.size()}")
            println("  Invoice Sheet: $invoiceSheetName")
            println("  Invoice Range: $invoiceRange")
            println("=" * 50)

            // Initialize Google Sheets service
            GoogleSheetsService sheetsService = new GoogleSheetsService(credentialsPath, spreadsheetId)
            println("✓ Google Sheets service initialized")

            // Initialize and start Discord bot
            DiscordBot bot = new DiscordBot(discordToken, sheetsService, commandPrefix, allowedUserIds, invoiceSheetName, invoiceRange)
            bot.start()

        } catch (Exception e) {
            System.err.println("Failed to start InvoiceBot:")
            System.err.println("  ${e.message}")
            e.printStackTrace()
            System.exit(1)
        }
    }
}
