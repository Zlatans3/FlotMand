package dk.zlatan.flotmand.util.easter_eggs

fun randomColor(): Int {
    val colors = listOf(
        0xFFE57373.toInt(), // Red
        0xFF81C784.toInt(), // Green
        0xFF64B5F6.toInt(), // Blue
        0xFFFFB74D.toInt(), // Orange
        0xFFBA68C8.toInt(), // Purple
        0xFFFF8A65.toInt(), // Deep Orange
        0xFF4DB6AC.toInt(), // Teal
        0xFFDCE775.toInt()  // Lime
    )
    return colors.random()
}