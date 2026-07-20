<div align="center">
  <picture>
    <source media="(prefers-color-scheme: dark)" srcset="https://github.com/user-attachments/assets/4af93d7e-e5d0-4734-9b07-abc62c735a06">
    <source media="(prefers-color-scheme: light)" srcset="https://github.com/user-attachments/assets/81c0c2c0-bc28-40de-aee6-d2d03f28bad2">
    <img width="250"  alt="flotmandapp" src="https://github.com/user-attachments/assets/81c0c2c0-bc28-40de-aee6-d2d03f28bad2">
  </picture>
</div>

![CI](https://github.com/zlatans3/FlotMand/actions/workflows/internal-release.yml/badge.svg)

<h1 align="center">
  👋 Hello FlotMand
</h1> 


This project marks a milestone between friends and that hard work can make hard problems go away! 


Flotmand started as an inside joke but later became a core part of our friendship slogan not to be interpreted as chauvinism. life and Everyone in it pretty and deserves to be recognized! 

This app is for 6 friends who month after month have celebrated each other's company by meeting up once a month for a dinner night.


The purpose of this app is to stay organised and have a helping tool making sure we never have to ask questions.

<BR><BR>

# ❓ What is this app about?

Concretely speaking this app lets us keep track of all our dinner night events, letting us know who's up next, where to meet up, who will participate and hopefully much more to come! 

if you are interested in following the roadmap, i made public [Trello board](https://trello.com/b/1Xu4SSJk/flotmand-app)


<BR><BR>

# ⚙️ What makes this app special? 

Unlike many other vibe coded app, This project is meant as an architectural playground, where i could set myself up to learn the many best practices and design patterns that the Android world covers while staying in the loop of the newest development that the Kotlin team is producing.

The aim is to create something scalable, easy to read and fun to use. 

<BR>
<BR> 

# 🎨 Design

Sinse i've got no internal branding and would like to make the switch from light to dark mode as easy as possible, material theming was the obvious choice here. 

I unfurtunetly hadn't had extra time to fit in any Figma design so here are some screenshots. 

<img width="250" alt="image" src="https://github.com/user-attachments/assets/bf0e4638-b020-4e08-9b3b-e074768ca787" />

<img width="250" alt="image" src="https://github.com/user-attachments/assets/fd01162c-2b55-460f-8a26-a14ea45df140" />

<img width="250" alt="image" src="https://github.com/user-attachments/assets/6dc01b48-218d-47de-b80d-47e72382e594" />

<img width="250" alt="image" src="https://github.com/user-attachments/assets/aef4bd1e-fb81-4efa-b595-668426470a31" />

<img width="250" alt="image" src="https://github.com/user-attachments/assets/0c319373-5dac-4128-aa71-49705778baf6" />

<img width="250" alt="image" src="https://github.com/user-attachments/assets/29a5bd24-f932-4e53-862c-bd88a6b298c3" />



## 🗺️ Adress lookup when creating event 

![lille](https://github.com/user-attachments/assets/585135dd-b50d-4d85-bec6-fc7d4c911072)

## 🗓️ Animated Ez Date polls

![afstemning gif](https://github.com/user-attachments/assets/5b5099a8-4dc1-46fc-8152-46ade2323f9d)

<BR><BR>

# ✍️ technical details 

I am hoping to add a lot of interesting entires to this list. Here you will be able to read about the interesting dependencies and technical stuff that has helped this project become what it is

- setup Hilt/ dagger for DI
- setup environment handling for both debug and release
- created my own custom implementation of a debug feature flag menu
- CI/CD pipeline for uploading new builds to internal test track on google play console
- Migrated app from Nav2 to Nav3
- Firebase Authenication OAuth using Google
- Firestore Database as Backend with CRUD
- Complete use of Material 3 theme and fully Dark/ light mode supported
- Displaying Profile images and linked URL's using Coil
- Firebase FCM Tokken based notifications using Node.js
- Google Maps SDK and Places API for searching addresses/ places and displaying them on a map

# 📍 Road Map 

I made a public trelloboard to keep track and display the current workflow of the app. During the next fase i would like to work on the following: 

- Refactor project to serve as a comunity hub, so other people can make use of event planning just like my friends and i
- Hoping to recreate this project as Compose multiplatform
- adding addresses and rejseplanen API
- Widget with Glance

# 🏗️ Structure 

this app is an Online first app starting with a login screen that lets you authenticate via Gmail through Firebase auth. From there you will land on the home screen with bottomnavigation to let you navigate through the app.

<BR><BR>

```mermaid
graph TD
      Login[Login] -->|Firebase auth| MainApp

      subgraph App Launch
          Login
      end

      subgraph Bottom Navigation
          FrontPage[Front Page]                                                                                             
          MyEvents[My Events]
          Profile[Profile]
      end

      MainApp --> FrontPage
      MainApp --> MyEvents
      MainApp --> Profile
  
      %% Front Page stack
      FrontPage --> Notif[Notifications]
      FrontPage -->|any event| FP_Detail[Event Detail]
      FrontPage --> Polls[Polls]

      Notif -->|event tap| FP_Detail
      Notif -->|poll tap| PollDetail

      Polls --> PollDetail[Poll Detail]                                                                                     
      PollDetail -->|create event| FP_CreateEvent[Create Event]
      FP_CreateEvent -->|event created| FP_Detail

      FP_Detail --> FP_Edit[Edit Event]

      %% My Events stack
      MyEvents --> ME_CreateEvent[Create Event]                                                                             
      MyEvents -->|own event| ME_Detail[Event Detail]
      ME_Detail --> ME_Edit[Edit Event]
      ME_CreateEvent -->|event created| ME_Detail

      %% Profile stack
      Profile -->|log out| Login                                                                                            
      Profile --> AccountInfo[Account Information]
      Profile --> Language[Language]
      Profile --> NotifSettings[Notification Settings]
      Profile --> Theme[Theme Settings]
      AccountInfo --> Licenses[Open Source Licenses]
````

# 👤 Can i use this app? 

As of now. Sadly not really.. Though i am planning to work on a system that lets any user create a group that they can invite they're friends to which would mean anyone would be able to use it 🌞










