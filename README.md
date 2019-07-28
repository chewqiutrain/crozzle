# Crozzle 
A Slack application for Cota's mini-cross-mini-club channel, and an attempt at learning Cats. The plan is to have a simple service (Crozzle) receive crossword puzzle times from Slack via slash commands, then persist those times along with usernames. This means our favourite analytics team member will no longer have to manually enter times into a spreadsheet, which should save considerable time given the recent increase in participation. 

The hope is that this data, together with crossword puzzle answers, will allow us to do some form of analysis into the breadth and depth of trivia knowledge among Co10s. Who knows? We might be able to create effective trivia teams!  

## Background
The mini-cross-mini-club channel at Cota is where Co10s note the time they took to solve the New York Times Daily Mini Crossword puzzle. 

## Docker
```bash
docker run --rm --name croz-pg -e POSTGRES_PASSWORD=12345 -d -p 127.0.0.1:15435:5432 -v $HOME/docker/volumes/postgres:/var/lib/postgresql/data postgres
```

## TODOs
1. Tidy up main loop
1. Slack return message composition, especially for Norden
1. CrobieInterpreter logging situation
1. Integration testing in code
1. Configurations
1. Dockerization
1. Get someone to help with dashboarding of data / analytics 
1. Web scraping of crossword puzzle solutions
1. Persistence of crossword puzzle solutions
