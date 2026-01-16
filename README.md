<div align="center">
  <picture>
    <source media="(prefers-color-scheme: dark)" srcset="https://github.com/user-attachments/assets/4af93d7e-e5d0-4734-9b07-abc62c735a06">
    <source media="(prefers-color-scheme: light)" srcset="https://github.com/user-attachments/assets/81c0c2c0-bc28-40de-aee6-d2d03f28bad2">
    <img width="250"  alt="flotmandapp" src="https://github.com/user-attachments/assets/81c0c2c0-bc28-40de-aee6-d2d03f28bad2">
  </picture>
</div>





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

Unlike many other vibe coded app, I strove for consistent and concise architecture. I also make sure to follow the best practices whether it is state handling navigation and logic in general. 
This is very loosely based on the Now in Android Repo. 

The aim is to make my little hobby project look like enterprice quality app material. 

<BR>
<BR>

# 🎨 Design

I love being creative, but designing is hard. I created a figma but most of it is screenshots for now, sinse i wanted quick results. 

[FIGMA](https://www.figma.com/design/ABW0FggMW6D8578ptpNgAx/Flotmand-App?node-id=0-1&t=AW1gJDiwbwg9VJYZ-1)

## 📱 Some nice looking screenshots

<img width="250" height="2424" alt="image" src="https://github.com/user-attachments/assets/a00794d6-fd7a-4974-a603-2bdfd94f93c6" />


<img width="250" height="2424" alt="image" src="https://github.com/user-attachments/assets/cf3ee348-9955-485b-bb0f-0bcd9847dfce" />


<img width="250" height="2424" alt="image" src="https://github.com/user-attachments/assets/14ccd8a8-c211-4c16-83f8-a8f555200e1c" />



## 🗺️ Adress lookup when creating event 

![lille](https://github.com/user-attachments/assets/585135dd-b50d-4d85-bec6-fc7d4c911072)

## 🗓️ Animated Ez Date polls

![afstemning gif](https://github.com/user-attachments/assets/5b5099a8-4dc1-46fc-8152-46ade2323f9d)

<BR><BR>

# ✍️ technical details 

I am hoping to add a lot of interesting entires to this list. Here you will be able to read about the interesting dependencies and technical stuff that has helped this project become what it is

- setup Hilt and dagger for DI
- Migrated app from Nav2 to Nav3
- Firebase Authenication With Google Login
- Firestore Database as Backend with CRUD
- Complete use of Material 3 theme and fully Dark/ light mode supported
- 

# 📍 Road Map 

- Hoping to recreate this project as Compose multiplatform
- Add retrofit support for adding addresses and rejseplanen API.
- Notifications for reminders
- Widget with Glance
- Using Google maps to show address
- Use of Room to save user data

# 🏗️ Structure 

Using MermaidJs to bring you a broad visual representation of how navigation is structered in this app. 

<BR><BR>

```mermaid
graph TD;
  Login[Login] --> B{Firebase};
  B --> |authentication| C[Front page];
  C --> D[My Events]
  C --> E[profile]
  C --> |Any event| G[Event details]
  D --> |CurrentUser event| G
  E --> |Log out| Login
  E --> j[Account Information]
  C --> H[Polls]
  H --> i{Poll details}
  D --> F[Create event]
subgraph Bottom Navigation
            C
            D
            E
end
subgraph App launch
            Login
end
````

# 👤 Can i use this app? 

As of now. Sadly not really.. Though i am planning to work on a system that lets any user create a group that they can invite they're friends to which would mean anyone would be able to use it 🌞










