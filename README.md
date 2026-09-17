# PRISM VISUALS

Клиентский визуальный мод для **Minecraft 1.21.1 (Fabric)**.
Чистая косметика: ничего не отправляет на сервер и не ломает механики —
только то, как игра выглядит и ощущается у тебя на экране.

## Функции (12 модулей)

| Модуль | Что делает |
| --- | --- |
| Hit Particles | Цветные частицы-всплеск при ударе по мобу (цвет / радуга / количество) |
| Full Bright | Максимальная яркость (гамма x1500), ночь как день |
| No Hurt Cam | Убирает тряску камеры при получении урона |
| Low Fire | Опускает текстуру огня на экране, когда горишь |
| Zoom | Плавный зум на клавишу **C** + кинематографичная камера |
| Toggle Sprint | Автоспринт по клавише **G** |
| Keystrokes | Оверлей клавиш WASD / ПКМ / ЛКМ / пробел + CPS |
| HUD Info | FPS, координаты, направление, биом, CPS, время |
| Armor HUD | Броня и прочность над хотбаром |
| Crosshair | Кастомный прицел: динамический разброс, пульс перезарядки |
| Block Overlay | Цветная обводка и заливка наводимого блока |
| Time Changer | Фиксированное клиентское время суток |

## Управление

- **Right Shift** — меню модулей (Prism GUI)
- **C** (зажать) — зум
- **G** — переключить автоспринт

## Сборка

### Способ 1 — в облаке, ничего не устанавливая (рекомендуется)

1. Залей все файлы проекта в новый репозиторий на https://github.com
   (кнопка **uploading an existing file** — прямо в браузере).
2. Перейди во вкладку **Actions** -> **Build Prism Visuals** -> **Run workflow**.
3. Через 3-6 минут скачай готовый JAR внизу страницы сборки
   (блок **Artifacts** -> **prism-visuals-jar**).
   Workflow уже готов и лежит в `.github/workflows/build.yml`.

### Способ 2 — на своём компьютере

1. Установи **JDK 21** (Temurin): https://adoptium.net/temurin/releases/?version=21
2. Установи **Gradle**: https://gradle.org/install/
3. В папке проекта выполни:
   ```
   gradle wrapper --gradle-version 8.10
   gradlew build
   ```
   На Windows: `gradlew.bat build`
4. Забери готовый мод: `build/libs/prism-visuals-1.0.0.jar`

## Установка

1. Установи **Fabric Loader** для 1.21.1: https://fabricmc.net/use/installer/
2. Скачай **Fabric API** (Modrinth) и брось в `.minecraft/mods`
3. Туда же — `prism-visuals-1.0.0.jar`
4. Запускай игру, жми **Right Shift**

Конфиг: `.minecraft/config/prism.json`
