# 🎲 discord-dice-bot

Discord에서 버튼/모달 UI로 주사위를 굴릴 수 있는 Java(JDA) 기반 봇 프로젝트입니다.

---

## 📌 프로젝트 목적
- 슬래시/텍스트 명령 기반 기능을 인터랙션 UI로 개선
- 범위 지정 랜덤 생성 로직과 입력 검증 로직 실습

## 🧱 기술 스택
- Java 17+
- JDA 5
- Gradle Wrapper

## ✨ 핵심 기능
- `!주사위` 명령으로 주사위 패널 출력
- 즉시 굴리기 버튼
- 범위 설정 모달(min/max)
- 입력값 검증(최소 1, 최대 999)

## ⚡ 실행 방법
```bash
cd discord-random-bot
./gradlew run
```
(Windows: `gradlew.bat run`)

## 📁 디렉토리 구조
- `discord-random-bot/src` : 봇 소스 코드
- `discord-random-bot/.env.example` : 환경변수 예시

## ✅ 상태
- 기본 주사위/범위 랜덤 동작 구현 완료
