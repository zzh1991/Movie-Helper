<p align="center">
    <a href="http://movie.zzhpro.com">
        <img src="./src/main/resources/static/favicon.ico" width="152">
    </a>
    <h3 align="center">Movie Helper</h3>
    <p align="center">
        <a href="https://github.com/zzh1991/Movie-Helper/blob/master/LICENSE"><img src="https://img.shields.io/github/license/zzh1991/Movie-Helper.svg"></a>
        <a href="#"><img src="https://img.shields.io/github/languages/top/zzh1991/Movie-Helper.svg"></a>
    </p>
    <p align="center">
        Movie management and find valueable movies to watch<br>
    </p>
</p>

## Git clone repo (optional)
Front-end project: [movie-helper-front](https://github.com/zzh1991/movie-helper-front)

```bash
# cd React-SpringBoot/
git clone https://github.com/zzh1991/movie-helper-front.git
```

#### Configure Front

- `cd movie-helper-front`
- `npm install`: install dependency
- prepare front static files
  - dev mode
    - `npm run dev`
  - Deploy mode
    - `npm run deploy`
- start the web app in IDE
- open browser
  - `dev mode`: go to http://localhost:3000
  - `deploy mode`: go to http://localhost:8080

### Update front end

```bash
cd movie-helper-front
git fetch origin master
git rebase origin/master
```

## Implement
### Back end
- Kotlin: refactor back end code
    1. easy to understand
    2. improve the code quality
- Spring Boot 3
- PostgresQL
- Guava
- Swagger
- Flyway

## Endpoints

### Swagger UI

- http://localhost:8080/swagger-ui/index.html

## Demo: [Movie Helper](http://movie.zzhpro.com)
### Recent Movies
![Recent](pictures/recent-movie.png)
### Top 100 Movies
![Top](pictures/top-movie.png)

## Author

👤 **Zhihao Zhang**

- Github: [@zzh1991](https://github.com/zzh1991)

## Show your support

Please ⭐️ this repository if this project helped you!

## 📝 License

Copyright © 2023 [zzh1991](https://github.com/zzh1991).<br />
This project is [MIT](https://github.com/zzh1991/Movie-Helper/blob/master/LICENSE) licensed.
