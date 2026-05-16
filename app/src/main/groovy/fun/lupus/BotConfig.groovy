package fun.lupus

/**
 * Configuration for the Discord bot
 */
class BotConfig {
    static String getDiscordToken() {
        String token = System.getenv('DISCORD_BOT_TOKEN')
        if (!token) {
            throw new IllegalStateException('DISCORD_BOT_TOKEN environment variable is not set')
        }
        return token
    }

    static String getGoogleSheetsCredentials() {
        return System.getenv('GOOGLE_CREDENTIALS_PATH') ?: 'credentials.json'
    }

    static String getSpreadsheetId() {
        String id = System.getenv('SPREADSHEET_ID')
        if (!id) {
            throw new IllegalStateException('SPREADSHEET_ID environment variable is not set')
        }
        return id
    }

    static String getCommandPrefix() {
        return System.getenv('COMMAND_PREFIX') ?: '!'
    }

    static Set<String> getAllowedUserIds() {
        String raw = System.getenv('ALLOWED_USER_IDS')
        if (!raw) {
            throw new IllegalStateException('ALLOWED_USER_IDS environment variable is not set')
        }
        return raw.split(',').collect { it.trim() }.findAll { it }.toSet()
    }
}
