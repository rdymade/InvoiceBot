package fun.lupus

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.requests.GatewayIntent
import java.time.LocalDateTime
import java.time.YearMonth
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

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
    private String defaultChannelId
    private JDA jda
    private ScheduledExecutorService scheduler

    DiscordBot(String token, GoogleSheetsService sheetsService, String commandPrefix, Set<String> allowedUserIds, String invoiceSheetName = 'Farm_list', String invoiceRange = 'E2:L', String defaultChannelId = null) {
        this.token = token
        this.sheetsService = sheetsService
        this.commandPrefix = commandPrefix
        this.allowedUserIds = allowedUserIds
        this.invoiceSheetName = invoiceSheetName
        this.invoiceRange = invoiceRange
        this.defaultChannelId = defaultChannelId
        this.scheduler = Executors.newScheduledThreadPool(1)
    }

    void start() {
        try {
            println("Starting Discord bot...")

            def listener = new DiscordCommandListener(sheetsService, commandPrefix, allowedUserIds, invoiceSheetName, invoiceRange)
            
            this.jda = JDABuilder.createDefault(token)
                    .enableIntents(
                            GatewayIntent.DIRECT_MESSAGES,
                            GatewayIntent.GUILD_MESSAGES,
                            GatewayIntent.MESSAGE_CONTENT
                    )
                    .addEventListeners(listener)
                    .build()

            jda.awaitReady()
            println("Discord bot is ready!")
            println("Invite URL: " + jda.getInviteUrl())

            // Initialize time-based automation
            if (defaultChannelId) {
                initializeAutomation()
            } else {
                println("⚠️  DEFAULT_CHANNEL_ID not set - time-based automation disabled")
            }

        } catch (Exception e) {
            System.err.println("Failed to start Discord bot: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }

    private void initializeAutomation() {
        println("Initializing time-based automation...")
        
        // Schedule tasks
        scheduleMonthlyInvoice()
        scheduleMonthlyReminder()
    }

    private void scheduleMonthlyInvoice() {
        // Calculate initial delay to next month start
        LocalDateTime now = LocalDateTime.now()
        LocalDateTime nextMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).plusMonths(1)
        
        long delaySeconds = java.time.temporal.ChronoUnit.SECONDS.between(now, nextMonth)
        
        println("Monthly invoice scheduled to run in ${delaySeconds} seconds (at $nextMonth)")
        
        // Run monthly on the 1st at midnight
        scheduler.scheduleAtFixedRate({
            executeMonthlyInvoice()
        }, delaySeconds, 30 * 24 * 60 * 60, TimeUnit.SECONDS) // Run every 30 days (approximate month)
    }

    private void scheduleMonthlyReminder() {
        // Calculate initial delay to 5 days before month end
        LocalDateTime now = LocalDateTime.now()
        YearMonth currentMonth = YearMonth.now()
        LocalDateTime nextMonthStart = currentMonth.plusMonths(1).atDay(1).atStartOfDay()
        LocalDateTime reminderTime = nextMonthStart.minusDays(5).withHour(9).withMinute(0).withSecond(0)
        
        // If reminder time is in the past, schedule for next month
        if (reminderTime.isBefore(now)) {
            reminderTime = reminderTime.plusMonths(1)
        }
        
        long delaySeconds = java.time.temporal.ChronoUnit.SECONDS.between(now, reminderTime)
        
        println("Monthly reminder scheduled to run in ${delaySeconds} seconds (at $reminderTime)")
        
        // Run every month, 5 days before month end
        scheduler.scheduleAtFixedRate({
            executeMonthlyReminder()
        }, delaySeconds, 30 * 24 * 60 * 60, TimeUnit.SECONDS) // Run every 30 days (approximate month)
    }

    private void executeMonthlyInvoice() {
        try {
            println("Executing monthly invoice automation...")
            
            def channel = jda.getTextChannelById(defaultChannelId)
            if (!channel) {
                System.err.println("Default channel with ID $defaultChannelId not found")
                return
            }
            
            // Get current month in YYYY-MM format
            String currentMonth = YearMonth.now().format(java.time.format.DateTimeFormatter.ofPattern('yyyy-MM'))
            
            // Fetch invoice data
            def invoiceData = sheetsService.fetchRange("'$invoiceSheetName'!$invoiceRange")
            
            if (invoiceData.isEmpty()) {
                channel.sendMessage("⚠️ No invoice data found in Google Sheets.").queue()
                return
            }
            
            def rows = invoiceData.drop(1)
            if (rows.isEmpty()) {
                channel.sendMessage("⚠️ No invoice rows found after the header row.").queue()
                return
            }
            
            boolean sentAny = false
            for (int i = 0; i < rows.size(); i++) {
                def row = rows[i]
                def validFlag = row.size() > 5 ? row[5]?.toString()?.trim() : ''
                if (!validFlag) {
                    continue
                }
                
                def character = row.size() > 2 ? row[2]?.toString()?.trim() : ''
                def reasonBase = row.size() > 3 ? row[3]?.toString()?.trim() : ''
                def amount = row.size() > 7 ? row[7]?.toString()?.trim() : ''
                
                if (!character || !amount) {
                    channel.sendMessage("⚠️ Skipping row ${i + 3}: missing character or amount.").queue()
                    continue
                }
                
                def reason = reasonBase ? "${reasonBase} ${currentMonth}" : currentMonth
                def invoiceCommand = "!new_invoice \"${character}\" \"${amount}\" \"${reason}\""
                channel.sendMessage(invoiceCommand).queue()
                sentAny = true
            }
            
            if (!sentAny) {
                channel.sendMessage("ℹ️ No valid invoice rows found with 'x' in column 5.").queue()
            }
            
            println("✓ Monthly invoice automation completed")
            
        } catch (Exception e) {
            System.err.println("Error executing monthly invoice: ${e.message}")
            e.printStackTrace()
        }
    }

    private void executeMonthlyReminder() {
        try {
            println("Executing monthly reminder automation...")
            
            def channel = jda.getTextChannelById(defaultChannelId)
            if (!channel) {
                System.err.println("Default channel with ID $defaultChannelId not found")
                return
            }
            
            // Get next month in YYYY-MM format
            String nextMonth = YearMonth.now().plusMonths(1).format(java.time.format.DateTimeFormatter.ofPattern('yyyy-MM'))
            
            def reminderMessage = """
            📋 **Monthly Invoice Reminder**
            
            Please check the Google Sheet for correctness before the automated invoice runs.
            The invoice will be generated for **$nextMonth** on the 1st of next month.
            """.stripIndent()
            
            channel.sendMessage(reminderMessage).queue()
            println("✓ Monthly reminder completed")
            
        } catch (Exception e) {
            System.err.println("Error executing monthly reminder: ${e.message}")
            e.printStackTrace()
        }
    }

    void shutdown() {
        if (scheduler) {
            scheduler.shutdown()
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow()
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow()
            }
        }
        
        if (jda) {
            jda.shutdown()
        }
    }
}
