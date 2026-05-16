package fun.lupus

import com.google.auth.oauth2.ServiceAccountCredentials
import groovy.json.JsonSlurper
import java.io.FileInputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Service for interacting with Google Sheets via REST API
 */
class GoogleSheetsService {
    private String spreadsheetId
    private String accessToken
    private static final String SHEETS_API_URL = 'https://sheets.googleapis.com/v4/spreadsheets'

    GoogleSheetsService(String credentialsPath, String spreadsheetId) {
        this.spreadsheetId = spreadsheetId
        this.accessToken = getAccessToken(credentialsPath)
    }

    private String getAccessToken(String credentialsPath) {
        try {
            def credentials = ServiceAccountCredentials
                    .fromStream(new FileInputStream(credentialsPath))
                    .createScoped(['https://www.googleapis.com/auth/spreadsheets.readonly'])

            // Get access token from credentials
            credentials.refresh()
            return credentials.accessToken.tokenValue
        } catch (Exception e) {
            throw new RuntimeException("Failed to get access token: ${e.message}", e)
        }
    }

    /**
     * Fetch data from a specific sheet range
     * @param range Format: 'Sheet1!A1:D10' or 'Sheet1'
     * @return List of rows, where each row is a List of values
     */
    List<List<Object>> fetchData(String range) {
        try {
            String url = "${SHEETS_API_URL}/${spreadsheetId}/values/${URLEncoder.encode(range, 'UTF-8')}"

            def connection = new URL(url).openConnection() as HttpURLConnection
            connection.setRequestMethod('GET')
            connection.setRequestProperty('Authorization', "Bearer ${accessToken}")
            connection.setRequestProperty('Accept', 'application/json')

            if (connection.responseCode == 200) {
                def response = connection.inputStream.text
                def json = new JsonSlurper().parseText(response)
                return json.values ?: []
            } else {
                // Try to read error body for more details
                String body = ''
                try {
                    body = connection.errorStream?.text ?: connection.inputStream?.text ?: ''
                } catch (e) {
                    body = "<unreadable>"
                }
                System.err.println("API Error: HTTP ${connection.responseCode}: ${body}")
                return []
            }
        } catch (Exception e) {
            System.err.println("Error fetching data from range $range: ${e.message}")
            return []
        }
    }

    /**
     * Fetch all data from a specific sheet
     * @param sheetName Name of the sheet (tab)
     * @return List of rows
     */
    List<List<Object>> fetchSheetData(String sheetName) {
        return fetchData("'$sheetName'!A:Z")
    }

    /**
     * Fetch data from an explicit sheet range
     * @param range Range string, e.g. 'Sheet1!A1:D10'
     * @return List of rows
     */
    List<List<Object>> fetchRange(String range) {
        return fetchData(range)
    }
}

