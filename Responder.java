import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.*;

/**
 * The responder class represents a response generator object.
 * It is used to generate an automatic response, based on specified input.
 * Input is presented to the responder as a set of words, and based on those
 * words the responder will generate a String that represents the response.
 *
 * Internally, the reponder uses a HashMap to associate words with response
 * strings and a list of default responses. If any of the input words is found
 * in the HashMap, the corresponding response is returned. If none of the input
 * words is recognized, one of the default responses is randomly chosen.
 * 
 * @author David J. Barnes and Michael Kölling.
 * @author Alejandro Olea
 * 
 * @version 2025.04.28
 */
public class Responder
{
    // Used to map key words to responses.
    private HashMap<String, String> responseMap;
    // Default responses to use if we don't recognise a word.
    private ArrayList<String> defaultResponses;
    // The name of the file containing the default responses.
    private static final String FILE_OF_DEFAULT_RESPONSES = "default.txt";
    // The name of the file containing the key response pairs.
    private static final String FILE_OF_RESPONSES = "responses.txt";
    private Random randomGenerator;

    /**
     * Construct a Responder
     */
    public Responder()
    {
        responseMap = new HashMap<>();
        defaultResponses = new ArrayList<>();
        randomGenerator = new Random();
        try {
            fillResponseMap();
            fillDefaultResponses();
        } catch (IOException e) {
            System.err.println("Error loading responses: " + e.getMessage());
            // Make sure we have at least one response.
            defaultResponses.add("Could you elaborate on that?");
        }
    }

    /**
     * Generate a response from a given set of input words.
     * 
     * @param words  A set of words entered by the user
     * @return       A string that should be displayed as the response
     */
    public String generateResponse(HashSet<String> words)
    {
        Iterator<String> it = words.iterator();
        while(it.hasNext()) {
            String word = it.next();
            String response = responseMap.get(word);
            if(response != null) {
                return response;
            }
        }
        // If we get here, none of the words from the input line was recognized.
        // In this case we pick one of our default responses (what we say when
        // we cannot think of anything else to say...)
        return pickDefaultResponse();
    }

    /**
     * Read key response pairs from responses.txt and enter them into our response map.
     * Comma-separated keys on the first line, followed by response lines,
     * with a blank line marking the end of a key-response pair.
     * Throws IOException if two or more consecutive blank lines are encountered.
     */
    private void fillResponseMap() throws IOException
    {
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_OF_RESPONSES))) {
            StringBuilder response = new StringBuilder();
            String[] keys = null;
            String line;
            int blankLineCount = 0;

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    blankLineCount++;
                    if (blankLineCount >= 2) {
                        throw new IOException("Encountered two or more consecutive blank lines in " + FILE_OF_RESPONSES);
                    }
                    if (keys != null && response.length() > 0) {
                        for (String key : keys) {
                            // Trim and remove quotes from keys (e.g., performance")
                            responseMap.put(key.trim().replace("\"", ""), response.toString());
                        }
                        keys = null;
                        response.setLength(0);
                    }
                } else {
                    blankLineCount = 0;
                    if (keys == null) {
                        keys = line.split(",");
                    } else {
                        if (response.length() > 0) {
                            response.append("\n");
                        }
                        response.append(line);
                    }
                }
            }
            // Handle the last key response pair
            if (keys != null && response.length() > 0) {
                for (String key : keys) {
                    responseMap.put(key.trim().replace("\"", ""), response.toString());
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println("Unable to open " + FILE_OF_RESPONSES);
        }
    }

    /**
     * Build up a list of default responses from default.txt.
     * Multi-line responses separated by blank lines.
     * Throws IOException if two or more consecutive blank lines are encountered.
     */
    private void fillDefaultResponses() throws IOException
    {
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_OF_DEFAULT_RESPONSES))) {
            StringBuilder response = new StringBuilder();
            String line;
            int blankLineCount = 0;

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    blankLineCount++;
                    if (blankLineCount >= 2) {
                        throw new IOException("Encountered two or more consecutive blank lines in " + FILE_OF_DEFAULT_RESPONSES);
                    }
                    if (response.length() > 0) {
                        defaultResponses.add(response.toString());
                        response.setLength(0);
                    }
                } else {
                    blankLineCount = 0;
                    if (response.length() > 0) {
                        response.append("\n");
                    }
                    response.append(line);
                }
            }
            // Handle the last response
            if (response.length() > 0) {
                defaultResponses.add(response.toString());
            }
        } catch (FileNotFoundException e) {
            System.err.println("Unable to open " + FILE_OF_DEFAULT_RESPONSES);
        }
        // Ensure at least one default response
        if (defaultResponses.size() == 0) {
            defaultResponses.add("Could you elaborate on that?");
        }
    }

    /**
     * Randomly select and return one of the default responses.
     * @return     A random default response
     */
    private String pickDefaultResponse()
    {
        // Pick a random number for the index in the default response list.
        // The number will be between 0 (inclusive) and the size of the list (exclusive).
        int index = randomGenerator.nextInt(defaultResponses.size());
        return defaultResponses.get(index);
    }
}
