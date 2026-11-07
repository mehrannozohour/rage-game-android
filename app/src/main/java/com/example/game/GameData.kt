package com.example.game

/**
 * Data definitions for Vector Rage stages, traps, obstacles, and sarcastic Persian taunts.
 */

data class StageFloor(
    val x: Float,
    val w: Float,
    val y: Float
)

enum class TrapType {
    VAULT_BOX,
    SPIKE_MIMIC_BOX,
    WALKING_BOX,
    SUDDEN_BRIDGE,
    CRUSHER,
    GLASS,
    FAKE_GLASS,
    BOOST_PAD,
    OVERHANG,
    LASER,
    ELEVATOR_LASER
}

data class StageTrap(
    val type: TrapType,
    var x: Float,
    val y: Float,
    val w: Float = 22f,
    val h: Float = 20f,
    val triggerDist: Float = 0f,
    val mode: String = "low", // "low" or "high"
    val endMode: String = "low",
    val vx: Float = 0f,
    var currentMode: String = mode,
    var triggered: Boolean = false,
    var broken: Boolean = false,
    var dropY: Float = 0f,
    var alertTimer: Float = 0f
)

data class StageData(
    val id: Int,
    val title: String,
    val warning: String,
    val speed: Float,
    val length: Float,
    val floors: List<StageFloor>,
    val traps: List<StageTrap>,
    val fakeExit: Boolean
)

data class Taunt(
    val emoji: String,
    val title: String,
    val desc: String
)

object GameData {
    fun toPersianDigits(num: Any): String {
        val s = num.toString()
        val faDigits = arrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
        val sb = StringBuilder()
        for (c in s) {
            if (c in '0'..'9') {
                sb.append(faDigits[c - '0'])
            } else {
                sb.append(c)
            }
        }
        return sb.toString()
    }

    fun formatTimer(seconds: Float): String {
        if (seconds < 0f) return "--:--"
        val totalHundredths = (seconds * 100).toInt()
        val sec = totalHundredths / 100
        val hundredths = totalHundredths % 100
        val mins = sec / 60
        val remainingSec = sec % 60
        return if (mins > 0) {
            String.format("%02d:%02d.%02d", mins, remainingSec, hundredths)
        } else {
            String.format("%02d.%02d", remainingSec, hundredths)
        }
    }

    fun formatTimerPersian(seconds: Float): String {
        return toPersianDigits(formatTimer(seconds))
    }

    val TAUNTS: Map<String, List<Taunt>> = mapOf(
        "elevator_laser" to listOf(
            Taunt("🤬⚡", "لیزر غافلگیرت کرد!", "لیزر به این گندگی تغییر ارتفاع داد و رفتی روش سوختی! واکنش سریع‌تری نشون بده!"),
            Taunt("🤡🔥", "تله لیزر دوقلو!", "فکر کردی الکیه؟ لیزر آسانسوری بالا و پایین میشه، باید سر می‌خوردی!"),
            Taunt("💩⚡", "گند زدی از سر تا پا!", "با این فرمون تا فردا هم رد نمی‌کنی! لیزر قشنگ برشته‌ت کرد!"),
            Taunt("🧠🤏", "مغز تعطیل و بی‌دقت!", "لیزر چشمک زد، آژیر کشید، باز رفتی مستقیم تو دلش؟!")
        ),
        "mimic_crate" to listOf(
            Taunt("📦🤬", "از یه جعبه کتک خوردی؟!", "جعبه پا درآورد و تیغ کشید! حواست به علامت تعجب قرمز بود؟"),
            Taunt("🤮📦", "جعبه‌های خائن!", "فکر کردی جعبه معمولیه که روش والت بزنی؟ تیغ کشید برات!"),
            Taunt("🤡🌵", "چه وضع بازی کردنه آخه؟!", "صبر کن اول تیغش رو ببین بعد بپر روش! دقت کن به حرکات جعبه!"),
            Taunt("🗑️🏃‍♂️", "تله متحرک!", "جعبه دونده با کله اومد تو صورتت! پرش بلند بزن ردش کن!")
        ),
        "sudden_gap" to listOf(
            Taunt("🕳️🤬", "پل ریخت و سقوط کردی!", "پل ریزشی لحظه آخر زیر پات خالی شد! باید دو پرشه می‌پریدی!"),
            Taunt("💩🦅", "پهن شدی ته دره!", "با چه رویی می‌خوای مراحل رو رد کنی وقتی سر یه چاله سقوط می‌کنی؟"),
            Taunt("🤦‍♂️💀", "آبرو هرچی گیمره بردی!", "کلید پرش رو نگه دار و دابل جامپ بزن تا نیفتی پایین!"),
            Taunt("🤡🕳️", "سقوط آزاد به ناکجاآباد!", "جاذبه زمین برنده شد! پرش به موقع راز زنده موندنه!")
        ),
        "crusher" to listOf(
            Taunt("🔨🤬", "له شدی مثل سوسک!", "زیر دستگاه پرس سنگین فرش پهن کردی نشستی؟ سریع‌تر تکل بزن!"),
            Taunt("🥞🤡", "کتلت پیکسلی تحویل بگیر!", "واکنش‌هات خیلی کنده! پرس سقفی از صد متری داد می‌زد!"),
            Taunt("🤕💩", "پرس هیدرولیک پیروز شد!", "دکمه سرعت و تکل رو بزن تا از زیرش قسر در بری!")
        ),
        "fake_exit" to listOf(
            Taunt("🏁🖕", "فکر کردی بردی؟! زرشک!", "دروازه خروج فرار کرد و لیزر مخفی جلو پات سبز شد!"),
            Taunt("🤡🎉", "زود جشن نگیر!", "هنوز مرحله تموم نشده بود نیشت باز شد! دروازه پرید عقب!"),
            Taunt("🚪🤬", "دروازه خائن فراری!", "دروازه گفت هنوز لیاقت رد کردن این مرحله رو نداری!")
        ),
        "hunter" to listOf(
            Taunt("👮‍♂️🤬", "مامور تعقیب دستگیرت کرد!", "انقدر به در و دیوار گیر کردی تا مامور با شوکر برقی بهت رسید!"),
            Taunt("🚓💩", "از مامور سنگین وزن خوردی؟!", "خیلی آروم دویدی! نباید به موانع گیر بکنی تا سرعتت کم نشه!"),
            Taunt("⚡🤡", "شوکر پلیس بهت چسبید!", "گیر کردی به مانع و شوکر خورد تو گردنت! روان بدو!")
        ),
        "spike_wall" to listOf(
            Taunt("🚀🤬", "با مخ رفتی تو سقف/دیوار!", "پد سرعت پرتابت کرد تو مانع سقفی! باید بلافاصله تکل می‌زدی!"),
            Taunt("🧱🤡", "پیشونیت از فولاده مگه؟!", "صد بار گفتم بعد پد سرعت سریع تکل برو!"),
            Taunt("💥💩", "تصادف با سرعت ۲۰۰ تا!", "سرعتت رفت بالا کنترل رو از دست دادی و کوبیدی به مانع!")
        )
    )

    val STREAK_ROASTS = listOf(
        "💬 مامور تعقیب: «درست بازی کن دیگه! خسته شدم از بس گرفتمت!»",
        "💬 سیستم هوش مصنوعی: «صفحه لمسی داره التماس می‌کنه، آرامش خودت رو حفظ کن!»",
        "💬 گزارش آزمایشگاه: «حرص نخور! واکنش انگشتات نیاز به تمرین داره!»",
        "💬 مامور تعقیب: «ناموساً چند بار می‌خوای سر یه تله بسوزی؟!»",
        "💬 سازنده بازی: «اگه گربه بازی می‌کرد تا الان مرحله رو رد کرده بود!»",
        "💬 تله‌های پشت‌بام: «بچه‌ها بخندین، دوباره اومد سر همون تله سوخت!»",
        "💬 سیستم بازی: «نفس عمیق بکش و دوباره دکمه شروع رو بزن!»",
        "💬 مامور تعقیب: «این شوکر رو بگیر خودت بزن، کار ما رو راحت کن!»",
        "💬 رکورد جهانی: «ثبت شدی به عنوان پر استقامت‌ترین بازیکن شکست‌خورده!»",
        "💬 راهنما: «یه لیوان آب خنک بخور، تمرکز کن و پرش‌های دقیق بزن!»"
    )

    val PIXEL_LAUGH_BUBBLES = listOf("NOOB!", "HAHA!", "TRASH", "LOSER", "EZ XD", "WIPED!")

    fun getStages(): List<StageData> {
        return listOf(
            // Stage 1
            StageData(
                id = 1,
                title = "پشت‌بام به ظاهر ساده!",
                warning = "از روی جعبه اول والت بزنید، شیشه را بشکنید، اما مراقب لیزر آخر باشید که لحظه آخر تغییر ارتفاع می‌دهد!",
                speed = 2.4f,
                length = 1380f,
                floors = listOf(
                    StageFloor(0f, 560f, 170f),
                    StageFloor(615f, 850f, 170f)
                ),
                traps = listOf(
                    StageTrap(TrapType.VAULT_BOX, 220f, 170f, w = 22f, h = 20f),
                    StageTrap(TrapType.GLASS, 370f, 170f, w = 8f, h = 48f),
                    StageTrap(TrapType.LASER, 500f, 170f, mode = "low", endMode = "low"),
                    StageTrap(TrapType.OVERHANG, 740f, 170f, w = 32f, h = 42f),
                    StageTrap(TrapType.ELEVATOR_LASER, 960f, 170f, mode = "high", endMode = "low", triggerDist = 92f),
                    StageTrap(TrapType.SUDDEN_BRIDGE, 1130f, 170f, w = 68f, triggerDist = 78f)
                ),
                fakeExit = false
            ),
            // Stage 2
            StageData(
                id = 2,
                title = "حمله جعبه‌های خائن",
                warning = "بعضی جعبه‌ها وقتی نزدیکشان می‌شوید تیغ درمی‌آورند یا به سمتتان می‌دوند! با پرش بلند از رویشان بپرید.",
                speed = 2.5f,
                length = 1480f,
                floors = listOf(
                    StageFloor(0f, 680f, 170f),
                    StageFloor(740f, 820f, 170f)
                ),
                traps = listOf(
                    StageTrap(TrapType.VAULT_BOX, 200f, 170f, w = 22f, h = 20f),
                    StageTrap(TrapType.SPIKE_MIMIC_BOX, 380f, 170f, w = 22f, h = 20f, triggerDist = 85f),
                    StageTrap(TrapType.OVERHANG, 550f, 170f, w = 34f, h = 42f),
                    StageTrap(TrapType.WALKING_BOX, 890f, 170f, w = 22f, h = 20f, triggerDist = 120f, vx = -1.6f),
                    StageTrap(TrapType.ELEVATOR_LASER, 1110f, 170f, mode = "low", endMode = "high", triggerDist = 95f),
                    StageTrap(TrapType.SPIKE_MIMIC_BOX, 1290f, 170f, w = 22f, h = 20f, triggerDist = 85f)
                ),
                fakeExit = false
            ),
            // Stage 3
            StageData(
                id = 3,
                title = "سقف و کف دروغگو",
                warning = "پد سرعت سبز شما را به سمت دیوار لیزری پرتاب می‌کند! بلافاصله بعد از پد تکل (سر خوردن) بزنید.",
                speed = 2.6f,
                length = 1560f,
                floors = listOf(
                    StageFloor(0f, 520f, 170f),
                    StageFloor(580f, 520f, 164f),
                    StageFloor(1160f, 500f, 170f)
                ),
                traps = listOf(
                    StageTrap(TrapType.BOOST_PAD, 230f, 170f, w = 28f),
                    StageTrap(TrapType.OVERHANG, 355f, 170f, w = 36f, h = 42f),
                    StageTrap(TrapType.SUDDEN_BRIDGE, 690f, 164f, w = 72f, triggerDist = 82f),
                    StageTrap(TrapType.CRUSHER, 890f, 164f, w = 28f, h = 36f, triggerDist = 88f),
                    StageTrap(TrapType.ELEVATOR_LASER, 1040f, 164f, mode = "high", endMode = "low", triggerDist = 95f),
                    StageTrap(TrapType.WALKING_BOX, 1350f, 170f, w = 22f, h = 20f, triggerDist = 125f, vx = -1.8f)
                ),
                fakeExit = false
            ),
            // Stage 4
            StageData(
                id = 4,
                title = "دروازه خروج فراری!",
                warning = "وقتی به دروازه سبز آخر مرحله می‌رسید، جشن نگیرید! دروازه فرار می‌کند و یک لیزر مخفی ظاهر می‌شود!",
                speed = 2.65f,
                length = 1520f,
                floors = listOf(
                    StageFloor(0f, 820f, 170f),
                    StageFloor(885f, 850f, 170f)
                ),
                traps = listOf(
                    StageTrap(TrapType.GLASS, 220f, 170f, w = 8f, h = 48f),
                    StageTrap(TrapType.ELEVATOR_LASER, 390f, 170f, mode = "high", endMode = "low", triggerDist = 95f),
                    StageTrap(TrapType.SPIKE_MIMIC_BOX, 580f, 170f, w = 22f, h = 20f, triggerDist = 88f),
                    StageTrap(TrapType.SUDDEN_BRIDGE, 720f, 170f, w = 70f, triggerDist = 85f),
                    StageTrap(TrapType.CRUSHER, 1040f, 170f, w = 28f, h = 36f, triggerDist = 90f),
                    StageTrap(TrapType.ELEVATOR_LASER, 1240f, 170f, mode = "low", endMode = "high", triggerDist = 95f)
                ),
                fakeExit = true
            ),
            // Stage 5
            StageData(
                id = 5,
                title = "پنجره فولادی و پرس سقفی",
                warning = "همه شیشه‌ها شکستنی نیستند! بعضی پنجره‌ها لحظه آخر تبدیل به دیوار فولادی می‌شوند و باید از رویشان بپرید!",
                speed = 2.7f,
                length = 1650f,
                floors = listOf(
                    StageFloor(0f, 740f, 170f),
                    StageFloor(805f, 960f, 170f)
                ),
                traps = listOf(
                    StageTrap(TrapType.GLASS, 230f, 170f, w = 8f, h = 48f),
                    StageTrap(TrapType.FAKE_GLASS, 430f, 170f, w = 12f, h = 30f, triggerDist = 90f),
                    StageTrap(TrapType.OVERHANG, 610f, 170f, w = 34f, h = 42f),
                    StageTrap(TrapType.BOOST_PAD, 890f, 170f, w = 28f),
                    StageTrap(TrapType.ELEVATOR_LASER, 1035f, 170f, mode = "low", endMode = "high", triggerDist = 105f),
                    StageTrap(TrapType.CRUSHER, 1230f, 170f, w = 28f, h = 36f, triggerDist = 92f),
                    StageTrap(TrapType.WALKING_BOX, 1450f, 170f, w = 22f, h = 20f, triggerDist = 130f, vx = -1.9f)
                ),
                fakeExit = false
            ),
            // Stage 6
            StageData(
                id = 6,
                title = "آسانسور لیزرهای دوقلو",
                warning = "دو لیزر پشت سر هم که یکی بالا می‌رود و دیگری پایین می‌آید! از سقوط سریع در هوا (تکل) استفاده کنید.",
                speed = 2.75f,
                length = 1720f,
                floors = listOf(
                    StageFloor(0f, 640f, 170f),
                    StageFloor(705f, 540f, 162f),
                    StageFloor(1310f, 520f, 170f)
                ),
                traps = listOf(
                    StageTrap(TrapType.ELEVATOR_LASER, 250f, 170f, mode = "high", endMode = "low", triggerDist = 96f),
                    StageTrap(TrapType.ELEVATOR_LASER, 430f, 170f, mode = "low", endMode = "high", triggerDist = 96f),
                    StageTrap(TrapType.SUDDEN_BRIDGE, 820f, 162f, w = 74f, triggerDist = 86f),
                    StageTrap(TrapType.SPIKE_MIMIC_BOX, 1020f, 162f, w = 22f, h = 20f, triggerDist = 90f),
                    StageTrap(TrapType.FAKE_GLASS, 1180f, 162f, w = 12f, h = 30f, triggerDist = 92f),
                    StageTrap(TrapType.ELEVATOR_LASER, 1490f, 170f, mode = "high", endMode = "low", triggerDist = 98f)
                ),
                fakeExit = false
            ),
            // Stage 7
            StageData(
                id = 7,
                title = "کارخانه پرس و شوکر",
                warning = "پرس‌های سقفی و جعبه‌های متحرک همزمان حمله می‌کنند! مامور امنیتی هم سریع‌تر تعقیبتان می‌کند.",
                speed = 2.82f,
                length = 1800f,
                floors = listOf(
                    StageFloor(0f, 860f, 170f),
                    StageFloor(930f, 980f, 170f)
                ),
                traps = listOf(
                    StageTrap(TrapType.CRUSHER, 240f, 170f, w = 28f, h = 36f, triggerDist = 92f),
                    StageTrap(TrapType.WALKING_BOX, 460f, 170f, w = 22f, h = 20f, triggerDist = 130f, vx = -1.9f),
                    StageTrap(TrapType.BOOST_PAD, 620f, 170f, w = 28f),
                    StageTrap(TrapType.OVERHANG, 755f, 170f, w = 36f, h = 42f),
                    StageTrap(TrapType.SUDDEN_BRIDGE, 1070f, 170f, w = 76f, triggerDist = 88f),
                    StageTrap(TrapType.ELEVATOR_LASER, 1290f, 170f, mode = "high", endMode = "low", triggerDist = 98f),
                    StageTrap(TrapType.CRUSHER, 1490f, 170f, w = 28f, h = 36f, triggerDist = 92f)
                ),
                fakeExit = true
            ),
            // Stage 8
            StageData(
                id = 8,
                title = "پشت‌بام روان‌پریش",
                warning = "هیچ الگوی ثابتی وجود ندارد! به علامت تعجب قرمز (!) بالای تله‌ها دقت کنید و سریع تصمیم بگیرید.",
                speed = 2.88f,
                length = 1880f,
                floors = listOf(
                    StageFloor(0f, 580f, 170f),
                    StageFloor(650f, 620f, 165f),
                    StageFloor(1340f, 680f, 170f)
                ),
                traps = listOf(
                    StageTrap(TrapType.SPIKE_MIMIC_BOX, 220f, 170f, w = 22f, h = 20f, triggerDist = 90f),
                    StageTrap(TrapType.ELEVATOR_LASER, 410f, 170f, mode = "low", endMode = "high", triggerDist = 98f),
                    StageTrap(TrapType.FAKE_GLASS, 770f, 165f, w = 12f, h = 30f, triggerDist = 92f),
                    StageTrap(TrapType.WALKING_BOX, 980f, 165f, w = 22f, h = 20f, triggerDist = 135f, vx = -2.0f),
                    StageTrap(TrapType.SUDDEN_BRIDGE, 1140f, 165f, w = 76f, triggerDist = 88f),
                    StageTrap(TrapType.BOOST_PAD, 1460f, 170f, w = 28f),
                    StageTrap(TrapType.OVERHANG, 1600f, 170f, w = 36f, h = 42f),
                    StageTrap(TrapType.ELEVATOR_LASER, 1760f, 170f, mode = "high", endMode = "low", triggerDist = 98f)
                ),
                fakeExit = false
            ),
            // Stage 9
            StageData(
                id = 9,
                title = "بزرگراه تله‌های زنجیره‌ای",
                warning = "سرعت بسیار بالاست! ترکیب پل‌های ریزشی، جعبه‌های دونده و دروازه خروج فراری!",
                speed = 2.95f,
                length = 1960f,
                floors = listOf(
                    StageFloor(0f, 720f, 170f),
                    StageFloor(790f, 620f, 164f),
                    StageFloor(1480f, 720f, 170f)
                ),
                traps = listOf(
                    StageTrap(TrapType.BOOST_PAD, 210f, 170f, w = 28f),
                    StageTrap(TrapType.ELEVATOR_LASER, 360f, 170f, mode = "low", endMode = "high", triggerDist = 105f),
                    StageTrap(TrapType.SUDDEN_BRIDGE, 540f, 170f, w = 76f, triggerDist = 90f),
                    StageTrap(TrapType.CRUSHER, 910f, 164f, w = 28f, h = 36f, triggerDist = 95f),
                    StageTrap(TrapType.SPIKE_MIMIC_BOX, 1110f, 164f, w = 22f, h = 20f, triggerDist = 92f),
                    StageTrap(TrapType.WALKING_BOX, 1310f, 164f, w = 22f, h = 20f, triggerDist = 135f, vx = -2.1f),
                    StageTrap(TrapType.FAKE_GLASS, 1640f, 170f, w = 12f, h = 30f, triggerDist = 95f),
                    StageTrap(TrapType.ELEVATOR_LASER, 1820f, 170f, mode = "high", endMode = "low", triggerDist = 100f)
                ),
                fakeExit = true
            ),
            // Stage 10
            StageData(
                id = 10,
                title = "فینال غیرممکن (کابوس نهایی)",
                warning = "مرحله دهم! تمام تله‌های بازی دست به یکی کرده‌اند تا نگذارند زنده به دروازه آزادی برسید!",
                speed = 3.05f,
                length = 2100f,
                floors = listOf(
                    StageFloor(0f, 680f, 170f),
                    StageFloor(755f, 680f, 165f),
                    StageFloor(1510f, 840f, 170f)
                ),
                traps = listOf(
                    StageTrap(TrapType.SPIKE_MIMIC_BOX, 210f, 170f, w = 22f, h = 20f, triggerDist = 92f),
                    StageTrap(TrapType.ELEVATOR_LASER, 390f, 170f, mode = "high", endMode = "low", triggerDist = 100f),
                    StageTrap(TrapType.SUDDEN_BRIDGE, 540f, 170f, w = 78f, triggerDist = 90f),
                    StageTrap(TrapType.BOOST_PAD, 840f, 165f, w = 28f),
                    StageTrap(TrapType.OVERHANG, 985f, 165f, w = 38f, h = 42f),
                    StageTrap(TrapType.CRUSHER, 1160f, 165f, w = 28f, h = 36f, triggerDist = 95f),
                    StageTrap(TrapType.WALKING_BOX, 1350f, 165f, w = 22f, h = 20f, triggerDist = 140f, vx = -2.2f),
                    StageTrap(TrapType.FAKE_GLASS, 1640f, 170f, w = 12f, h = 30f, triggerDist = 95f),
                    StageTrap(TrapType.ELEVATOR_LASER, 1810f, 170f, mode = "low", endMode = "high", triggerDist = 102f),
                    StageTrap(TrapType.ELEVATOR_LASER, 1960f, 170f, mode = "high", endMode = "low", triggerDist = 102f)
                ),
                fakeExit = true
            )
        )
    }
}
