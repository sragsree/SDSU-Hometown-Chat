# Hometown Chat

Hometown is a basically a chat application that helps students to locate people in their hometown who goes to SDSU. This app communicates with SDSU bismarck Server to retrieve students details and firebase for saving chat history.The app has 

  - List view
  - Map View
  - Chat View  (WhatsApp like View)

The application is extremely fast and responsive as caching is implemented with SQLite database. Only authorised users can start a conversation. Authentication is implemented using firebase.List View displays the list of users in with their nickname, city, state and country and year of study at SDSU. clicking on a partiular user in the list will start a chat with him. Map View plots all users in google map with markers. clicking on a particular marker title will start a conversation with him/her. You can also apply filters like country, state and year to locate a particular user.
### Installation

The application can be run using the apk on any device running android 4.3 and above
For contibuting to the development or to view the source code one has to install Android Studio. The installation is as follows

  - Download Android Studio from https://developer.android.com/studio/index.html
  - Launch the .exe file you downloaded.
  - Follow the setup wizard to install Android Studio and any necessary SDK tools.
  - This application can be run in Emulator or on an actual device running Android 4.4 or above.

### Usage

```
public class ChatActivity extends AppCompatActivity {
    static final String TAG = ChatActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        // User login
        if (ParseUser.getCurrentUser() != null) { // start with existing user
            startWithCurrentUser();
        } else { // If not logged in, login as a new anonymous user
            login();
        }
    }
```    

### Contributing

1. Fork it!
2. Create your feature branch: `git checkout -b my-new-feature`
3. Commit your changes: `git commit -am 'Add some feature'`
4. Push to the branch: `git push origin my-new-feature`
5. Submit a pull request :D

License
----

**Free Software, Hell Yeah!**
