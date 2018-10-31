Oktay Sen s1663938
# Coinz Project Plan
## Design Decisions
* **Game**
  * **Collecting coins**
    * A "coin" is an item representing an amount of cryptocurrency that can be found within The University of Edinburgh campus area.
    * There are 4 main cryptocurrencies in this game: PENY, DOLR, SHIL, QUID. Their worth is determined relative to a 5th cryptocurrency called GOLD, which is only used for this purpose.
    * Each coin has a location and some value in terms of PENY, DOLR, SHIL or QUID. A coin cannot have values in multiple cryptocurrencies.
    * Each coin will expire 24 hours after they were generated.
    * While the app is open, when a player's current location is 25 metres of a coin, the coin is automatically collected.
    * Once collected, a coin will disappear from the map and won't be collectible by any other player.
  * **Banking**
    * The coins a player has collected is stored in their "wallet". A wallet is a coin inventory, where each coin occupies a single slot.
    * Coins in a wallet keep their expiration date, and will disappear from the wallet within 24 hours of its creation.
    * Every player has a bank account, which is also a coin inventory where each coin occupies a single slot.
    * A coin can be deposited from the player's wallet into their bank account, up to 25 times a day. Deposited coins no longer have an expiration date, and can be stored in a bank account indefinitely.
    * Each coin with its cryptocurrency value is stored in the bank account separately, and can be withdrawn at any time without limits.
  * **_(Additional Feature)_ Trading**
    * Coins stored in either the player's wallet or bank account can be traded with another player.
    * A trade is exchange of items between two players. Coins and other items may be exchanged in this way.
    * To trade with another player, a player must first initiade a trade offer. In a trade offer, a player specifies zero or more items from their inventory, and zero or more items from the other player's inventory. If the trade offer is accepted by the other player, the items specified will swap owners.
    * Trade offers can be sent to any player that can be found on the leaderboard (see below).
    * Once a trade request is sent, the receiving player will get a notification about it. The receiving player accepting or rejecting the offer will also send a notification to the sending player.
    * Coins traded in this way end up being stored in the wallet of the new owner, regardless of which inventory the coin was initially in. 
  * **_(Additional Feature)_ Shop**
    * Players have access to a shop where they can buy various items using their coins.
    * Non-coin items are stored in the player's "Inventory", and are publicly visible along with their wallet and bank account.
    * The shop contains cosmetic items that players can use to customize their character.
    * Each item in the shop has a price expressed in one of PENY, DOLR, SHIL or QUID. To purchase an item, the player needs to have coins in that cryptocurrency with value greater than or equal to the price of the item. Once a coin is used in a purchase, it disappears. Players can use coins in their wallets or bank accounts for purchasing.
    * The shop also offers services to make it easier to shop. In the shop, players can break their coins up to multiple, lesser valued coins, or convert a coin's cryptocurrency into another based on today's rate. These services aren't free, however, so player's are incentivised to trade in order to avoid having to pay extra fees.
  * **_(Additional Feature)_ Leaderboard**
    * Every player is listed in a global leaderboard, sorted by their "net worth".
    * Net worth of a player is calculated by converting all of their current coins and the purchase prices of the items in their inventory into GOLD and adding them up.
    * Players can see the whole leaderboard, though the ranking of them and the players they traded with before are highlighted.
* **Technologies**
  * The app will be made using the Android's native platform and written in Kotlin. The choice of Kotlin over Java is explained below.
  * Google Firebase will be used to provide the back-end services necessary.
  * Authentication will be achieved using Firebase Auth, and users will be able to use their email & a password or their existing Google account to create and log into the app. After account creation, users will then be able to choose a unique username.
  * Storage of various kinds of data, such as players' bank account, wallet and inventory, and trade requests that are ongoing or happened in the past will be stored in Firebase Cloud Firestore.
  * Image assets for various items in the shop will be stored in Firebase Storage.
  * Trade notifications will be sent using Firebase Cloud Messaging. Since Firebase Cloud Messaging requires a back-end, Firebase Cloud Functions will be used to send notifications whenever a trade request is created/updated. Firebase Cloud Functions will be written in Kotlin as per coursework requirements.
  * The location and value of coins, and the conversion rate of the cryptocurrencies compared to GOLD are acquired from a University of Edinburgh API, which is updated daily.
  * To track conversion rate changes over time and to track the availability of coins on the map, requests to the University of Edinburgh API will be made using Firebase Cloud Functions, and stored in Firebase Cloud Firestore.
  * To display a map with locations of the player and coins, Mapbox will be used.
* **User Interface**
  * Upon the initial launch of the app, a login screen will be displayed with the option to login or register using email/password or Google.
  * Once the player has logged in, they will be greeted with a screen with 5 tabs: Map, Inventory, Shop, Trading and Leaderboard. The Map tab is open by default, and the user can easily switch between tabs by using the buttons on screen.
  * The Map tab contains a Mapbox map with markers displaying locations of coins and the player's current location. Each marker is circular in shape, have a different color depending on which cryptocurrency it's value is based on, and display a number which is the floor of the cryptocurrency value it contains.
  * The Inventory tab has options for displaying the player's wallet, bank account or inventory. For each inventory type, the items in the inventory are displayed in a grid layout. For coins, along with an image of the coin, the cryptocurrency value and the GOLD value in today's exchange rate is displayed. For non-coin items, only an image of the item and the name of the item is displayed. Interacting with an item will provide options to exchange it to a different cryptocurrency in the shop (if the item is a coin) or trade it with another player.
  * The shop tab also has the option to view items for sale, or view services. Items for sale are displayed in a grid, the same way they're displayed in the Inventory tab. The only difference is that each item also has a price in a cryptocurrency. Clicking on an item will provide a selection screen for selecting one or more coins from the player's wallet and bank account to spend on purchasing the item. The services option displays a different interface with the option to select a coin to break up or exchange. Today's exchange rate and a graph displaying the change of exchange rates over the last 30 days is also displayed.
  * Trading tab is split into two panels on the left and right. The left panel displays a list of players the player has past/current trade requests with. Once a player on the left panel is selected, the right panel displays the history of trade requests with the player and the status of these trade requests. There's also a button to initiate a new trade request with the selected player. Tapping this button reveals an inventory selection screen displaying both players' inventories, bank accounts and wallets.
  * Leaderboard screen displays a list of players and their net worth. By default, the list is centered on the player's own place in the leaderboard, but options for displaying only the top players and players the user had past trade requests with in the past are also available. Clicking on a player will reveal options to see their inventory or initiate a trade request with them.
## Language of Choice
For Android Development, both Java and Kotlin are great choices. Thanks to the interoperability between these languages, they're both equally capable, so choosing either of them wouldn't be incorrect. That said, I'm choosing Kotlin for this project, and here's why.

Kotlin is a functional programming language, meaning functions are a valid type, unlike Java. In my personal experience, I found functional programming to be a great way to write code. It makes code more readable, and promotes best practices like immutability. While Java also has functional programming capabilities thanks to Java 8, it has significant limitations that arise from Java not being a functional language in nature. 

Kotlin is also much more concise than Java. In Java, even the simple tasks such as creating a class require a lot of effort due to how verbose Java is. It also makes reading and understanding Java code more difficult. Kotlin has a lot of tricks up it's sleeve that lets it express the same ideas without as much clutter around it, which means we can develop faster and write more readable code.

Another Java problem Kotlin fixes is the type "null". Unlike Java, Kotlin types are not nullable by default, and we can modify any type to include null if we'd like. This means we avoid a ton of problems by encountering them in compile-time rather than run-time.

With any new language, though, an important thing to consider is the developer support for the language. It can be quite risky to build a large project with a new language if the language still contains bugs, or there aren't a lot of resources for help available. This isn't as much of a problem with Kotlin though. Kotlin is developed and supported by JetBrains, which is a pretty big name to have behind a programming language. Also, for our specific task of building an Android app, Google is also supporting Kotlin as it's now one of the official languages supported for Android development. Lastly, because of Kotlin's interoperability with Java, any resources found for Java can also be used for Kotlin by converting the source code. This all means that Kotlin is stable enough for our project that we shouldn't encounter many problems due to how young Kotlin is.

## Timetable
| Week | Task |
| -- | -- |
| Week 1-2 | Focus on other classes and part-time work. |
| Week 3 | Look into the coursework specification and think about ideas. Look into Kotlin vs Java. |
| Week 4 | Create a blank Android project and the Github repository. Finalize the decisions about the project and write this report. |
| Week 5 | Initialize a Firebase project, integrate Firebase and Mapbox into the app. Set up unit testing within the app, and make sure interaction with Firebase is testable. Write tests for every new interaction with Firebase from now on. |
| Week 6 | Implement end-to-end where Cloud Functions make daily requests to the UoE API, store the data in Cloud Firestore, and make the Mapbox map displays coins read from the database. |
| Week 7 | Implement the login screen and Firebase Auth. Implement the logic for collecting coins upon approach and recording it into the database in the wallet. |
| Week 8 | Make the UI for the Inventory tab, and display the coins collected in the past. |
| Week 9 | Make the Trading tab, secure trading logic and Cloud Messaging to send notifications. |
| Week 10 | Make the Shop & Leaderboard tabs. As the core systems will be in place at this point, they shouldn't take too much time to implement. |
| Week 11-13 | Time to document code, polish, fix bugs, implement missing tests or features etc. Hopefully will be done early to leave room to focus on finals. |
