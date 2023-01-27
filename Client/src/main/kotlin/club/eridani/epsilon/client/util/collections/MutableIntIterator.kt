package club.eridani.epsilon.client.util.collections

interface MutableIntIterator : MutableIterator<Int> {
    override fun next(): Int {
        return nextInt()
    }

    fun nextInt(): Int
}