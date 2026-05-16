package fun.lupus

import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.requests.GatewayIntent

/**
 * Main Discord bot class
 */
class DiscordBot {
    private String token
    private GoogleSheetsService sheetsService
    private String commandPrefix
    private Set<String> allowedUserIds
    private String invoiceSheetName
    private String invoiceRange

    DiscordBot(String token, GoogleSheetsService sheetsService, String commandPrefix, Set<String> allowedUserIds, String invoiceSheetName = 'Farm_list', String invoiceRange = 'E2:L') {
        this.token = token
        this.sheetsService = sheetsService
        this.commandPrefix = commandPrefix
        this.allowedUserIds = allowedUserIds
        this.invoiceSheetName = invoiceSheetName
        this.invoiceRange = invoiceRange
    }

    void start() {
        try {
            println("Starting Discord bot...")

            def jda = JDABuilder.createDefault(token)
                    .enableIntents(
                            GatewayIntent.DIRECT_MESSAGES,
                            GatewayIntent.GUILD_MESSAGES,
                            GatewayIntent.MESSAGE_CONTENT
                    )
                    .addEventListeners(new DiscordCommandListener(sheetsService, commandPrefix, allowedUserIds, invoiceSheetName, invoiceRange))
                    .build()

            jda.awaitReady()
            println("Discord bot is ready!")
            println("Invite URL: " + jda.getInviteUrl())

        } catch (Exception e) {
            System.err.println("Failed to start Discord bot: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }
}
