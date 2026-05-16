package fun.lupus

import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

/**
 * Listener for Discord message commands
 */
class DiscordCommandListener extends ListenerAdapter {
    private GoogleSheetsService sheetsService
    private String commandPrefix
    private Set<String> allowedUserIds
    private String invoiceSheetName
    private String invoiceRange

    DiscordCommandListener(GoogleSheetsService sheetsService, String commandPrefix = '!', Set<String> allowedUserIds = [] as Set, String invoiceSheetName = 'Farm_list', String invoiceRange = 'E2:L') {
        this.sheetsService = sheetsService
        this.commandPrefix = commandPrefix
        this.allowedUserIds = allowedUserIds
        this.invoiceSheetName = invoiceSheetName
        this.invoiceRange = invoiceRange
    }

    @Override
    void onMessageReceived(MessageReceivedEvent event) {
        // Ignore bot messages
        if (event.author.isBot()) return

        // Ignore messages from users not on the allow list
        if (!allowedUserIds.contains(event.author.id)) return

        String message = event.message.contentRaw

        // Check if message is a command
        if (!message.startsWith(commandPrefix)) return

        String[] args = message.substring(commandPrefix.length()).split(' ')
        String command = args[0].toLowerCase()

        switch (command) {
            case 'invoice':
                handleInvoiceCommand(event, args)
                break
            case 'data':
                handleDataCommand(event, args)
                break
            case 'help':
                handleHelpCommand(event)
                break
            default:
                event.channel.sendMessage("Unknown command: `$command`. Type `${commandPrefix}help` for available commands.").queue()
        }
    }

    private void handleInvoiceCommand(MessageReceivedEvent event, String[] args) {
        try {
            if (args.size() != 2) {
                event.channel.sendMessage("Usage: `${commandPrefix}invoice YYYY-MM`").queue()
                return
            }

            String suffix = args[1]?.trim()
            if (!suffix || !(suffix ==~ /^\d{4}-\d{2}$/)) {
                event.channel.sendMessage("Suffix is required and must be in format YYYY-MM. Example: `${commandPrefix}invoice 2026-05`").queue()
                return
            }

            def invoiceData = sheetsService.fetchRange("'$invoiceSheetName'!$invoiceRange")

            if (invoiceData.isEmpty()) {
                event.channel.sendMessage("No invoice data found in Google Sheets.").queue()
                return
            }

            def rows = invoiceData.drop(1)
            if (rows.isEmpty()) {
                event.channel.sendMessage("No invoice rows found after the header row.").queue()
                return
            }

            boolean sentAny = false
            for (int i = 0; i < rows.size(); i++) {
                def row = rows[i]
                def validFlag = row.size() > 5 ? row[5]?.toString()?.trim() : ''
                if (!validFlag) {
                    continue
                }

                def character = row.size() > 0 ? row[0]?.toString()?.trim() : ''
                def reasonBase = row.size() > 3 ? row[3]?.toString()?.trim() : ''
                def amount = row.size() > 7 ? row[7]?.toString()?.trim() : ''

                if (!character || !amount) {
                    event.channel.sendMessage("Skipping row ${i + 3}: missing character or amount.").queue()
                    continue
                }

                def reason = reasonBase
                if (suffix) {
                    reason = reason ? "${reason} ${suffix}" : suffix
                }

                def invoiceCommand = "/new_invoice character: ${character} amount: ${amount} reason: ${reason}"
                event.channel.sendMessage(invoiceCommand).queue()
                sentAny = true
            }

            if (!sentAny) {
                event.channel.sendMessage("No valid invoice rows found with 'x' in column 5.").queue()
            }

        } catch (Exception e) {
            event.channel.sendMessage("Error fetching invoice data: ${e.message}").queue()
            e.printStackTrace()
        }
    }

    private void handleDataCommand(MessageReceivedEvent event, String[] args) {
        try {
            if (args.size() < 2) {
                event.channel.sendMessage("Usage: `${commandPrefix}data <sheet_name>`").queue()
                return
            }

            String sheetName = args[1]
            def data = sheetsService.fetchSheetData(sheetName)

            if (data.isEmpty()) {
                event.channel.sendMessage("No data found in sheet: `$sheetName`").queue()
                return
            }

            // Build response
            def response = new StringBuilder()
            response.append("📊 **Data from $sheetName**\n```\n")
            
            data.each { row ->
                response.append(row.join(' | ')).append('\n')
            }
            
            response.append("```")

            if (response.length() <= 2000) {
                event.channel.sendMessage(response.toString()).queue()
            } else {
                event.channel.sendMessage("Data is too large. Try specifying a smaller range.").queue()
            }

        } catch (Exception e) {
            event.channel.sendMessage("Error fetching data: ${e.message}").queue()
        }
    }

    private void handleHelpCommand(MessageReceivedEvent event) {
        def helpMessage = """
        🤖 **InvoiceBot Commands**
        
        `${commandPrefix}invoice [reason-suffix]` - Generate one `/new_invoice` command per row
        `${commandPrefix}data <sheet_name>` - Fetch data from a specific sheet
        `${commandPrefix}help` - Show this help message
        """.stripIndent()

        event.channel.sendMessage(helpMessage).queue()
    }
}
