# Guess Game


## Technologies Used

- **Java 17** | 17.0.8-tem
- **Gradle 8.7** for building the project
- **Spring** for runtime framework
- **Web Socket** for connection
- **Lombok** for reducing boilerplate code
- **Validation** for input validation

### Testing Tools:
- **SpringTest** for testing framework

### Other:

- **Sdkman 5.18** - as system configs
- **Kubuntu 24** - as OS
---

## Setup and Run Instructions

- Ensure `:gradle` is installed with compatible version.
- `application.yml` contains editable variables.
- Reachable by default `localhost:8080/game/guess`.
### Run by IDE

- Run Application by IDE.

### Run by Gradle

- Execute in terminal command `gradle bootRun` 

### Run by docker

- Execute in terminal command `docker build -t guess-game-server . && docker run -p 8080:8080 guess-game-server`

### Run by docker-compose

- Execute in terminal command `docker-compose up --build`

---

## Interaction instruction


- On first connection UUID is given as plain text, this uuid is used in each request as `validationUUID` while session is alive. And default uniq nickname is given.
- Nickname can be overwritten by valid bet request, on each bet player can overwrite to new nickname, or once - then it will be kept until session ends.
- Response on each request is given as enum in String format.
- On WIN/LOSE player will receive data in JSON format.
- After each round, if there are players that won, all players that are connected to session wil receive List of players that won in JSON Format.

## Examples:

### Connection
```
    949c8c39-66e8-4a33-aa63-c218ef3842a5
```
- UUID as String
### Bet Request
```
{
    "validationUUID" : "949c8c39-66e8-4a33-aa63-c218ef3842a5",
    "number" : 7,
    "amount" : 12.00,
    "nickname" : "Chuck Norris"
}
```
- `validationUUD` - UUID as String
- `number` - integer
- `amount` - double
- `nickname`- (Optional) String

### Bet Result
```
{
    "betResult": "WIN",
    "betNumber": 7,
    "betAmount": 12.0,
    "winNumber": 7,
    "winAmount": 118.8
}
```
- `betResult` - ( WIN | LOSE ) enum in String
- `betNumber` - integer
- `betAmount` - double
- `winNumber` - integer
- `winAmount` - (Optional) shown if won bet

### List of Winners
```
[
    {
        "nickname": "Player_e2cac577-c215-4ad2-81a2-fb7d218b5e20",
        "winAmount": 99
    },
    {
        "nickname": "Chuck Norris",
        "winAmount":118.8
    }
]
```
- `nickname` - string
- `winAmount` - double

### Enum Messages

- `TIMEOUT` - no bets for some period
- `BAD_REQUEST` - not valid request
- `BETS_LIMIT` - this round already stacked a bet
- `BET_ACCEPTED` - bet received and pending till round ends

---

## Info

👤 Creator:
Toomas Park

📧 toomas.park.work@gmail.com

---
### Test task for Yolo Group

---