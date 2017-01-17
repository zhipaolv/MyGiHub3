/**
 * Created by Administrator on 2017/1/10.
 */


import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.client.repackaged.org.apache.commons.codec.binary.Base64;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartBody;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.*;
import java.io.*;
import java.util.*;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import javax.mail.MessagingException;


public class Quickstart {
    /** Application name. */
    private static final String APPLICATION_NAME =
            "Gmail API Java Quickstart";

    /** Directory to store user credentials for this application. */
    private static final java.io.File DATA_STORE_DIR = new java.io.File(
            System.getProperty("user.home"), ".credentials/gmail-java-quickstart");

    /** Global instance of the {@link FileDataStoreFactory}. */
    private static FileDataStoreFactory DATA_STORE_FACTORY;

    /** Global instance of the JSON factory. */
    private static final JsonFactory JSON_FACTORY =
            JacksonFactory.getDefaultInstance();

    /** Global instance of the HTTP transport. */
    private static HttpTransport HTTP_TRANSPORT;

    /** Global instance of the scopes required by this quickstart.
     *
     * If modifying these scopes, delete your previously saved credentials
     * at ~/.credentials/gmail-java-quickstart
     */
    private static final List<String> SCOPES =
            Arrays.asList(GmailScopes.GMAIL_LABELS,GmailScopes.GMAIL_READONLY);

    static {
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Creates an authorized Credential object.
     * @return an authorized Credential object.
     * @throws IOException
     */
    private static Credential authorize() throws IOException {
        // Load client secrets.
        InputStream in =
                Quickstart.class.getResourceAsStream("/client_secret.json");
        GoogleClientSecrets clientSecrets =
                GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow =
                new GoogleAuthorizationCodeFlow.Builder(
                        HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                        .setDataStoreFactory(DATA_STORE_FACTORY)
                        .setAccessType("offline")
                        .build();
        Credential credential = new AuthorizationCodeInstalledApp(
                flow, new LocalServerReceiver()).authorize("user");
        System.out.println(
                "Credentials saved to " + DATA_STORE_DIR.getAbsolutePath());
        return credential;
    }

    /**
     * Build and return an authorized Gmail client service.
     * @return an authorized Gmail client service
     * @throws IOException
     */
    private static Gmail getGmailService() throws IOException {
        Credential credential = authorize();
        return new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    public static void main(String[] args) throws IOException, MessagingException{
        // Build a new authorized API client service.
        if(args.length != 1 || args[0].indexOf("@") == -1){
            System.out.println("请输入正确的邮件地址！");
        } else {
            String user = args[0];
            String query = "label:inbox newer_than:4d";
            Gmail service = getGmailService();

            // get the list message in the user's account.


            List<String> str = Arrays.asList("Subject");
            ListMessagesResponse response = service.users().messages().list(user).setQ(query).execute();
            List<Message> messages = new ArrayList<Message>();
            while (response.getMessages() != null) {
                messages.addAll(response.getMessages());
                if (response.getNextPageToken() != null) {
                    String pageToken = response.getNextPageToken();
                    response = service.users().messages().list(user).setQ(query).setPageToken(pageToken).execute();
                } else {
                    break;
                }
            }

            //print the title
            int i = 1;
            for (Message message : messages) {
                Message mess = service.users().messages().get(user, message.getId()).execute();
                Message mess2 = service.users().messages().get(user, message.getId()).setFormat("metadata").setMetadataHeaders(str).execute();

                //输出邮件标题和内容


                System.out.println("邮件" + i);
                System.out.println("标题：" + mess2.getPayload().getHeaders().get(0).getValue());
                System.out.println("邮件内容");
                StringBuilder stringBuilder = new StringBuilder();
                List<MessagePart> parts = mess.getPayload().getParts();

                getPlainTextFromMessageParts(parts, stringBuilder);
                byte[] bodyBytes = Base64.decodeBase64(stringBuilder.toString());
                String text = new String(bodyBytes, "UTF-8");

                System.out.println(text);

                String fileaddr = "\\IDEA\\" + user + "\\" ;

                for (MessagePart part : parts) {
                    if (part.getFilename() != null && part.getFilename().length() > 0) {
                        String filename = part.getFilename();
                        String attId = part.getBody().getAttachmentId();
                        MessagePartBody attachPart = service.users().messages().attachments().
                                get(user, message.getId(),attId).execute();


                        org.apache.commons.codec.binary.Base64 base64Url = new org.apache.commons.codec.binary.Base64(true);
                        byte[] fileByteArray = base64Url.decodeBase64(attachPart.getData());
                        File addr = new File(fileaddr);
                        if(!addr.exists()){
                            addr.mkdir();
                        }
                        FileOutputStream fileOutFile =
                                new FileOutputStream(fileaddr + filename);
                        fileOutFile.write(fileByteArray);
                        fileOutFile.close();
                    }
                }
                i++;

            }
        }
    }

    private static void getPlainTextFromMessageParts(List<MessagePart> messageParts, StringBuilder stringBuilder) {
        for (MessagePart messagePart : messageParts) {
            if (messagePart.getMimeType().equals("text/plain")) {
                stringBuilder.append(messagePart.getBody().getData());
            }

            if (messagePart.getParts() != null) {
                getPlainTextFromMessageParts(messagePart.getParts(), stringBuilder);
            }
        }
    }
}
