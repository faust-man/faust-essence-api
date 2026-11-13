# Essence API

Независимый API серии Essence для Minecraft Java **1.21.1**, JDK **21**,
Fabric и NeoForge. Версия **1.0.0**, mod ID **essence_api**.

Содержит контракты характеристик, вкладок, навыков, практики, событий, ресурсов и
клиентского HUD. Core реализует прогрессию и базовую ветку. API не зависит от Core
и самостоятельно не добавляет игровые механики.

Сборка: `./gradlew.bat build`. Сначала соберите API, затем соседний `core`, затем
дополнения. Каждый установленный мод остаётся отдельным JAR; API нужен на клиенте
и сервере. [Контракты, примеры и проверки](docs/API.md).

- Fabric: `fabric/build/libs/essence-api-fabric-1.21.1-1.0.0.jar`
- NeoForge: `neoforge/build/libs/essence-api-neoforge-1.21.1-1.0.0.jar`

Gradle Wrapper и схема двух загрузчиков основаны на рабочем проекте Essence Core
(MultiLoader-Template). Исходные уведомления и лицензия сохранены в LICENSE.
