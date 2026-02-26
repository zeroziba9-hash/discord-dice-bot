# discord-random-bot

초간단 디스코드 랜덤 숫자 봇 (Java 17 + JDA 5)

## 기능
- `!랜덤` → 1~10
- `!랜덤 최소 최대` → 지정 범위 랜덤

## 실행 방법 (PowerShell)

1) 토큰 설정
```powershell
$env:DISCORD_TOKEN='여기에_디스코드_봇_토큰'
```

2) 실행
```powershell
./gradlew run
```

> gradlew가 없다면 IntelliJ에서 Gradle 프로젝트로 열고 `Main.main()` 실행해도 됨.

## 디스코드 포털 설정
- Bot > Privileged Gateway Intents > **MESSAGE CONTENT INTENT** 켜기
- 봇을 서버에 초대할 때 `bot` scope 포함
