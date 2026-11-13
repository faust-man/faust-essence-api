# Essence API 1.0

Minecraft Java 1.21.1, JDK 21. Пакет `com.faust.essence.api`, mod ID `essence_api`.
Один отдельный JAR API нужен на клиенте и сервере рядом с Core и дополнениями.
API не содержит Core и не зависит от него. Без Core доступны регистрация и
проверка `EssenceApi.available()`, но операции прогрессии требуют backend Core.
Шесть базовых характеристик объявляет `CoreSkills` внутри Core; API не добавляет
их самостоятельно. Старый `com.faust.core.api.CoreApi` удалён; дополнения нужно
пересобрать. Формат идентификаторов и сохранённый прогресс игроков не меняются.

## Сборка и подключение

Разместите репозитории рядом: `faust-essence-api`, `core`, `faust-essence-stamina`,
`epic-fight compat`. Сначала выполните `./gradlew.bat build` в API, затем в Core,
затем в дополнениях. В остальных проектах путь API можно задать
`-PapiProjectDir=<путь>`. Для Stamina сохраняется `-PcoreProjectDir=<путь>`.
Готовые JAR API: `fabric/build/libs/essence-api-fabric-1.21.1-1.0.0.jar` и
`neoforge/build/libs/essence-api-neoforge-1.21.1-1.0.0.jar`.

У зависимого мода обязательная зависимость `essence_api >=1.0.0` на обеих сторонах;
в NeoForge укажите `ordering="AFTER"`. API не нужно вкладывать внутрь JAR дополнения.
Для компиляции используйте JAR своего загрузчика, для Fabric — через
`modImplementation`, чтобы Loom применил mappings. В API нет Fabric API-зависимости.

## Регистрация

Регистрацию выполняйте во время инициализации мода. ID новых характеристик,
навыков, эффектов и действий должны иметь namespace. Не регистрируйте повторно
один и тот же ID при каждом входе игрока или перезагрузке конфига.

```java
import com.faust.essence.api.*;
import java.util.List;

var lore = EssenceApi.registerSkill("example:lore");
EssenceApi.registerBonus("example:insight", (player, value) -> {
    // Применение эффекта: value — сумма активных навыков с этим эффектом.
});
Extensions.tab(new Extensions.Tab("example", 1, 1, false));
Extensions.characteristic(new Extensions.Characteristic(lore, 3, 20, 1, 100, 20, 2));
Extensions.action(new Extensions.Action("example:study", lore, 1, "example:books"));
Extensions.ability(new Extensions.Ability("example:reader",
    List.of(new Extensions.Requirement(lore, 1)),
    List.of(new Extensions.Condition("example:books", 5)), List.of(),
    "example:insight", 1, true, 0, 0, "minecraft:book"));
```

`Tab`: namespace, базовая цена, шаг цены, интеграция в Core. Счётчик покупок
отдельный для каждого namespace. `Characteristic`: характеристика, начальный и
максимальный пределы, шаг предела, базовая цена уровня в опыте, её рост за уровень,
максимальный множитель роста. Поддерживаемый максимум покупаемого предела — 20.
Обычный конструктор Ability требует всех родителей и всех условий. Полный
конструктор также поддерживает anyParents, requiredParents, excludes и
requiredConditions. Нулевой requiredConditions означает все условия; нулевой
requiredParents при непустом anyParents означает одного любого родителя.
Координаты узлов — целые единицы дерева Core; ядро сохраняет безопасный зазор.

Core преобразует дескрипторы в свою конфигурацию и добавляет отсутствующие записи
при загрузке; существующие пользовательские настройки сохраняются. API не
предоставляет прямого доступа к Balance, Progress или сетевым пакетам.

## Серверные операции

Вызывайте из серверного потока. Вызов из другого потока отклоняется.

```java
EssenceApi.grantAction(player, "example:study", 1, 1);
EssenceApi.recordCounter(player, "example:books", 1);
EssenceApi.recordMaximum(player, "example:record", 10);
boolean needed = EssenceApi.needsCounter(player, "example:books");
var view = EssenceApi.snapshot(player);
var strength = view.tracks().get(Skill.valueOf("strength"));
boolean learned = EssenceApi.learned(player, "example:reader");
boolean enabled = EssenceApi.enabled(player, "example:reader");
EssenceApi.unlock(player, "example:reader");
EssenceApi.toggle(player, "example:reader");
EssenceApi.buyCap(player, lore);
```

Снимок и его коллекции неизменяемые и отделены от сохранения игрока. Track содержит
уровень, текущий и требуемый опыт, предел, цену повышения и множитель роста.
Открытие навыка проверяет реальные условия; покупка списывает уровни ванильного
опыта. `grantExperience` выдаёт сырой опыт характеристики с её правилами роста;
для игровых действий предпочтителен `grantAction` с отдельным правилом практики.
`registerPath` и `choosePath` предоставляют выбор пути с пользовательским условием.

`beforeProgress` меняет множитель или отменяет награду; `afterProgress` получает
снимки до и после успешной награды за действие. `onStateChange` вызывается при
синхронизации изменившегося прогресса, включая уровни и состояния навыков;
начальная синхронизация событие изменения не создаёт. `onConfigurationReload`
сообщает об успешной загрузке конфигурации Core. Подписчики вызываются синхронно;
не выполняйте в них блокирующие операции.

## Ресурсы и клиент

`Resources.find(id)` ищет провайдер ресурса через ServiceLoader;
`Resources.stamina()` — необязательный ресурс Stamina. ResourceProvider предоставляет
снимок, проверку расхода, оплату, восстановление и фактически потраченное количество.

```java
boolean paid = Resources.payStamina(player, 1, true); // требуется полная единица
boolean lastAction = Resources.payStamina(player, 1, false); // допустим остаток
```

Без Stamina `payStamina` пропускает действие без списания. Нулевые цены допустимы,
отрицательные и нечисловые суммы отклоняются. В Stamina сохраняются прежние правила
творческого режима, истощения, задержки восстановления и последнего действия.
Новый ресурс реализует ResourceProvider и объявляет имя реализации в
`META-INF/services/com.faust.essence.api.ResourceProvider`.

Только на клиенте используйте `com.faust.essence.api.client.ClientApi`: read-only
снимок синхронизированного прогресса, активности навыков и выносливости. До первой
синхронизации возвращается пустой Optional. HUD-провайдер реализует StaminaClient
и объявляется в одноимённом META-INF/services. Серверные фасады не обнаруживают
клиентские провайдеры и не загружают классы GUI.

## Граница реализации

EssenceBackend реализует Core. StaminaResource реализует ресурсный контракт API.
Внутренние StaminaMechanics, StaminaModel и адаптеры конфигурации Epic Fight остаются
внутри реализации своих модов: они интегрируют конкретную механику, а не являются
публичным контрактом для новых дополнений. Новым потребителям не требуется
обращаться к CoreRuntime.data, Progress или Balance для перечисленных операций.

## Проверки

`./gradlew.bat build` проверяет оба загрузчика и запускает независимые контрактные
проверки без Core. `./gradlew.bat :neoforge:runServer -PruntimeChecks` проверяет
сервер с одним API и автоматически останавливает тестовый процесс. EULA принимает
владелец сервера. Серверные проверки адаптера с Core и Stamina находятся в
Compat: `./gradlew.bat build runServer -PrevisionChecks -PwithStamina`.
