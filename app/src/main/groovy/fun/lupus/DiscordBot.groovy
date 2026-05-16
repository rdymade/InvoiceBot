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

    DiscordBot(String token, GoogleSheetsService sheetsService, String commandPrefix, Set<String> allowedUserIds) {
        this.token = token
        this.sheetsService = sheetsService
        this.commandPrefix = commandPrefix
        this.allowedUserIds = allowedUserIds
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
                    .addEventListeners(new DiscordCommandListener(sheetsService, commandPrefix, allowedUserIds))
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
