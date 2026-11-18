package com.example.model

import androidx.annotation.Keep
import androidx.compose.ui.graphics.Color
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

enum class AppSection {
  CHATS,
  PROFILE,
}

enum class ProfileTab(val title: String) {
  GIFTS("Подарки"),
  FRAMES("Рамки"),
  MARKET("Маркет"),
}

enum class AvatarFrameStyle(
  val title: String,
  val subtitle: String,
  val primaryColor: Color,
  val secondaryColor: Color,
  val priceCrystals: Int,
) {
  NEON_PULSE(
    title = "Neon Spike",
    subtitle = "Энергия неона",
    primaryColor = Color(0xFF38BDF8),
    secondaryColor = Color(0xFFA855F7),
    priceCrystals = 0,
  ),
  CYBER_HALO(
    title = "Cyber Katana",
    subtitle = "Орбита киберпанка",
    primaryColor = Color(0xFF22D3EE),
    secondaryColor = Color(0xFFEC4899),
    priceCrystals = 0,
  ),
  ICE_CUBE(
    title = "Ice Cube",
    subtitle = "Морозная аура",
    primaryColor = Color(0xFF7DD3FC),
    secondaryColor = Color(0xFF0284C7),
    priceCrystals = 0,
  ),
  SAKURA_BLOOM(
    title = "Hearts Bloom",
    subtitle = "Лепестки сакуры",
    primaryColor = Color(0xFFF472B6),
    secondaryColor = Color(0xFFFB7185),
    priceCrystals = 0,
  ),
  GOLD_LAUREL(
    title = "Gold Laurel",
    subtitle = "Золотой венок",
    primaryColor = Color(0xFFFBBF24),
    secondaryColor = Color(0xFFF59E0B),
    priceCrystals = 0,
  ),
  LIGHTSABER(
    title = "Lightsabers",
    subtitle = "Плазменный ореол",
    primaryColor = Color(0xFF34D399),
    secondaryColor = Color(0xFF3B82F6),
    priceCrystals = 0,
  ),
}

enum class ChatBackdrop(val title: String, val color: Color) {
  NONE("Без обоев", Color(0xFF111317)),
  PAPER("Бумага", Color(0xFF181B20)),
  SAGE("Шалфей", Color(0xFF131D1A)),
  SKY("Небо", Color(0xFF121A26)),
  SAND("Песок", Color(0xFF1D1913)),
  POWDER("Пудра", Color(0xFF1E151C)),
  SLATE("Сланец", Color(0xFF161922)),
}

enum class ChatPattern(val title: String) {
  NONE("Нет"),
  DOTS("Точки"),
  GRID("Сетка"),
  STROKES("Штрихи"),
  WAVES("Волны"),
  PLUSES("Плюсы"),
}

enum class ChatAccent(val title: String, val color: Color, val bubbleBg: Color) {
  BLUE("Telegram", Color(0xFF3390EC), Color(0xFF244365)),
  CYAN("Неон", Color(0xFF38BDF8), Color(0xFF1C445D)),
  EMERALD("Изумруд", Color(0xFF10B981), Color(0xFF153E32)),
  PURPLE("Индиго", Color(0xFFA855F7), Color(0xFF3C2259)),
  PINK("Сакура", Color(0xFFEC4899), Color(0xFF531E3B)),
  AMBER("Янтарь", Color(0xFFF59E0B), Color(0xFF4C3414)),
}

@Keep
@IgnoreExtraProperties
data class UserProfileDoc(
  @DocumentId val id: String = "",
  val userId: String = "",
  val displayName: String = "Benzo",
  val username: String = "@Benzo",
  val bio: String = "Молодой Миллионер",
  val birthday: String = "15 июня 2004 г.",
  val crystals: Long = 97L,
  val selectedFrame: String = AvatarFrameStyle.NEON_PULSE.name,
  val chatBackdrop: String = ChatBackdrop.SLATE.name,
  val chatPattern: String = ChatPattern.PLUSES.name,
  val chatAccent: String = ChatAccent.BLUE.name,
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
) {
  val frameEnum: AvatarFrameStyle
    get() = AvatarFrameStyle.entries.firstOrNull { it.name == selectedFrame } ?: AvatarFrameStyle.NEON_PULSE

  val backdropEnum: ChatBackdrop
    get() = ChatBackdrop.entries.firstOrNull { it.name == chatBackdrop } ?: ChatBackdrop.SLATE

  val patternEnum: ChatPattern
    get() = ChatPattern.entries.firstOrNull { it.name == chatPattern } ?: ChatPattern.PLUSES

  val accentEnum: ChatAccent
    get() = ChatAccent.entries.firstOrNull { it.name == chatAccent } ?: ChatAccent.BLUE
}

@Keep
@IgnoreExtraProperties
data class OwnedGiftDoc(
  @DocumentId val id: String = "",
  val userId: String = "",
  val fragmentSlug: String = "",
  val number: Long = 1L,
  val name: String = "",
  val modelName: String = "",
  val modelRarityPercent: String = "2%",
  val backgroundName: String = "Sapphire",
  val backgroundRarityPercent: String = "1.5%",
  val patternName: String = "Telegram",
  val patternRarityPercent: String = "5%",
  val editionText: String = "1/500 выпущено",
  val owner: String = "Benzo",
  val priceRub: Long = 0L,
  val priceCrystals: Long = 0L,
  val upgraded: Boolean = false,
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
) {
  fun toNftGift(): NftGift {
    val palette = SampleNftCatalog.paletteForSlug(fragmentSlug)
    return NftGift(
      id = id,
      fragmentSlug = fragmentSlug,
      number = number.toInt(),
      name = name,
      modelName = modelName,
      modelRarityPercent = modelRarityPercent,
      backgroundName = backgroundName,
      backgroundRarityPercent = backgroundRarityPercent,
      patternName = patternName,
      patternRarityPercent = patternRarityPercent,
      editionText = editionText,
      owner = owner,
      priceRub = priceRub.toInt(),
      priceCrystals = priceCrystals.toInt(),
      bgStartColor = palette.first,
      bgEndColor = palette.second,
      accentColor = palette.third,
      isUpgraded = upgraded,
    )
  }
}

@Keep
@IgnoreExtraProperties
data class ChatThreadDoc(
  @DocumentId val id: String = "",
  val userId: String = "",
  val title: String = "",
  val subtitle: String = "в сети",
  val avatarText: String = "E",
  val lastMessage: String = "",
  val pinned: Boolean = false,
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
)

@Keep
@IgnoreExtraProperties
data class ChatMessageDoc(
  @DocumentId val id: String = "",
  val userId: String = "",
  val chatId: String = "",
  val senderName: String = "",
  val text: String = "",
  val outgoing: Boolean = true,
  val giftSlug: String? = null,
  val giftTitle: String? = null,
  @ServerTimestamp val createdAt: Timestamp? = null,
)

data class NftGift(
  val id: String,
  val fragmentSlug: String,
  val number: Int,
  val name: String,
  val modelName: String,
  val modelRarityPercent: String,
  val backgroundName: String,
  val backgroundRarityPercent: String,
  val patternName: String,
  val patternRarityPercent: String,
  val editionText: String,
  val owner: String,
  val priceRub: Int,
  val priceCrystals: Int,
  val bgStartColor: Color,
  val bgEndColor: Color,
  val accentColor: Color,
  val isUpgraded: Boolean = false,
) {
  val fullTitle: String
    get() = "$name #$number"

  val imageUrl: String
    get() = "https://nft.fragment.com/gift/$fragmentSlug.medium.jpg"

  val largeImageUrl: String
    get() = "https://nft.fragment.com/gift/$fragmentSlug.large.jpg"

  val stickerWebpUrl: String
    get() = "https://nft.fragment.com/gift/$fragmentSlug.webp"
}

data class CollectibleUsername(
  val username: String = "@crow",
  val owner: String = "Benzo",
  val acquiredDate: String = "17 нояб. 2026 г.",
  val priceCrystals: Int = 500,
  val priceRub: Int = 675,
)

object SampleNftCatalog {
  fun paletteForSlug(slug: String): Triple<Color, Color, Color> =
    when {
      slug.startsWith("vintagecigar") -> Triple(Color(0xFF535B66), Color(0xFF2E333B), Color(0xFF94A3B8))
      slug.startsWith("plushpepe") -> Triple(Color(0xFF065F46), Color(0xFF062C23), Color(0xFF34D399))
      slug.startsWith("durovscap") -> Triple(Color(0xFF1E4D8C), Color(0xFF102547), Color(0xFF38BDF8))
      slug.startsWith("iongem") -> Triple(Color(0xFF4C1D95), Color(0xFF1E1B4B), Color(0xFF34D399))
      slug.startsWith("sakuraflower") -> Triple(Color(0xFF831843), Color(0xFF3B0D22), Color(0xFFF472B6))
      slug.startsWith("springbasket") -> Triple(Color(0xFF0F766E), Color(0xFF082F49), Color(0xFF5EEAD4))
      slug.startsWith("preciouspeach") -> Triple(Color(0xFF9A3412), Color(0xFF431407), Color(0xFFFB923C))
      slug.startsWith("astralshard") -> Triple(Color(0xFF1E3A8A), Color(0xFF0F172A), Color(0xFF60A5FA))
      slug.startsWith("spyagaric") -> Triple(Color(0xFF9D174D), Color(0xFF4C0519), Color(0xFFF43F5E))
      else -> Triple(Color(0xFF6B21A8), Color(0xFF2E1065), Color(0xFFE879F9))
    }

  val initialOwnedGifts =
    listOf(
      NftGift(
        id = "cigar_1",
        fragmentSlug = "vintagecigar-1",
        number = 1,
        name = "Vintage Cigar",
        modelName = "Spectral Smoke",
        modelRarityPercent = "2%",
        backgroundName = "Battleship Grey",
        backgroundRarityPercent = "1.37%",
        patternName = "Viking",
        patternRarityPercent = "8.33%",
        editionText = "1/450 выпущено",
        owner = "Benzo",
        priceRub = 3400,
        priceCrystals = 180,
        bgStartColor = Color(0xFF535B66),
        bgEndColor = Color(0xFF2E333B),
        accentColor = Color(0xFF94A3B8),
      ),
      NftGift(
        id = "pepe_1",
        fragmentSlug = "plushpepe-1",
        number = 1,
        name = "Plush Pepe",
        modelName = "Midas Pepe",
        modelRarityPercent = "0.5%",
        backgroundName = "Emerald",
        backgroundRarityPercent = "1.20%",
        patternName = "Crown",
        patternRarityPercent = "2.10%",
        editionText = "1/2800 выпущено",
        owner = "Benzo",
        priceRub = 145000,
        priceCrystals = 990,
        bgStartColor = Color(0xFF065F46),
        bgEndColor = Color(0xFF062C23),
        accentColor = Color(0xFF34D399),
      ),
      NftGift(
        id = "cap_1",
        fragmentSlug = "durovscap-1",
        number = 1,
        name = "Durov's Cap",
        modelName = "Classic Black",
        modelRarityPercent = "1.5%",
        backgroundName = "Sapphire",
        backgroundRarityPercent = "1.80%",
        patternName = "Telegram",
        patternRarityPercent = "3.20%",
        editionText = "1/5000 выпущено",
        owner = "Benzo",
        priceRub = 28500,
        priceCrystals = 420,
        bgStartColor = Color(0xFF1E4D8C),
        bgEndColor = Color(0xFF102547),
        accentColor = Color(0xFF38BDF8),
      ),
      NftGift(
        id = "gem_3",
        fragmentSlug = "iongem-3",
        number = 3,
        name = "Ion Gem",
        modelName = "Prism Core",
        modelRarityPercent = "0.9%",
        backgroundName = "Cyber Violet",
        backgroundRarityPercent = "1.12%",
        patternName = "Crystal Grid",
        patternRarityPercent = "3.50%",
        editionText = "3/250 выпущено",
        owner = "Benzo",
        priceRub = 12400,
        priceCrystals = 310,
        bgStartColor = Color(0xFF4C1D95),
        bgEndColor = Color(0xFF1E1B4B),
        accentColor = Color(0xFF34D399),
      ),
      NftGift(
        id = "sakura_5",
        fragmentSlug = "sakuraflower-5",
        number = 5,
        name = "Sakura Flower",
        modelName = "Spring Bloom",
        modelRarityPercent = "3.1%",
        backgroundName = "Rose Dusk",
        backgroundRarityPercent = "2.40%",
        patternName = "Petals",
        patternRarityPercent = "6.10%",
        editionText = "5/1000 выпущено",
        owner = "Benzo",
        priceRub = 1850,
        priceCrystals = 55,
        bgStartColor = Color(0xFF831843),
        bgEndColor = Color(0xFF3B0D22),
        accentColor = Color(0xFFF472B6),
      ),
      NftGift(
        id = "basket_2",
        fragmentSlug = "springbasket-2",
        number = 2,
        name = "Spring Basket",
        modelName = "Easter Gold",
        modelRarityPercent = "2.4%",
        backgroundName = "Emerald Moss",
        backgroundRarityPercent = "2.15%",
        patternName = "Clover",
        patternRarityPercent = "5.80%",
        editionText = "2/750 выпущено",
        owner = "Benzo",
        priceRub = 2200,
        priceCrystals = 70,
        bgStartColor = Color(0xFF0F766E),
        bgEndColor = Color(0xFF082F49),
        accentColor = Color(0xFF5EEAD4),
      ),
    )

  val marketGifts =
    listOf(
      NftGift(
        id = "peach_1",
        fragmentSlug = "preciouspeach-1",
        number = 1,
        name = "Precious Peach",
        modelName = "999 Karat",
        modelRarityPercent = "1.1%",
        backgroundName = "Amber Sunset",
        backgroundRarityPercent = "1.45%",
        patternName = "Gold Leaf",
        patternRarityPercent = "4.80%",
        editionText = "1/3000 выпущено",
        owner = "Маркет",
        priceRub = 9500,
        priceCrystals = 35,
        bgStartColor = Color(0xFF9A3412),
        bgEndColor = Color(0xFF431407),
        accentColor = Color(0xFFFB923C),
      ),
      NftGift(
        id = "shard_1",
        fragmentSlug = "astralshard-1",
        number = 1,
        name = "Astral Shard",
        modelName = "Nebula Core",
        modelRarityPercent = "0.8%",
        backgroundName = "Midnight Indigo",
        backgroundRarityPercent = "1.05%",
        patternName = "Stardust",
        patternRarityPercent = "3.90%",
        editionText = "1/1500 выпущено",
        owner = "Маркет",
        priceRub = 14200,
        priceCrystals = 45,
        bgStartColor = Color(0xFF1E3A8A),
        bgEndColor = Color(0xFF0F172A),
        accentColor = Color(0xFF60A5FA),
      ),
      NftGift(
        id = "agaric_1",
        fragmentSlug = "spyagaric-1",
        number = 1,
        name = "Spy Agaric",
        modelName = "Toxic Neon",
        modelRarityPercent = "2.5%",
        backgroundName = "Crimson Glow",
        backgroundRarityPercent = "2.90%",
        patternName = "Spores",
        patternRarityPercent = "7.20%",
        editionText = "1/4000 выпущено",
        owner = "Маркет",
        priceRub = 1400,
        priceCrystals = 25,
        bgStartColor = Color(0xFF9D174D),
        bgEndColor = Color(0xFF4C0519),
        accentColor = Color(0xFFF43F5E),
      ),
      NftGift(
        id = "cake_1",
        fragmentSlug = "homemadecake-1",
        number = 1,
        name = "Homemade Cake",
        modelName = "Berry Frost",
        modelRarityPercent = "3.2%",
        backgroundName = "Royal Plum",
        backgroundRarityPercent = "2.37%",
        patternName = "Candles",
        patternRarityPercent = "8.33%",
        editionText = "1/6000 выпущено",
        owner = "Маркет",
        priceRub = 850,
        priceCrystals = 15,
        bgStartColor = Color(0xFF6B21A8),
        bgEndColor = Color(0xFF2E1065),
        accentColor = Color(0xFFE879F9),
      ),
    )
}
