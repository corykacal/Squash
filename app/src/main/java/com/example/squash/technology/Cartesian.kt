package com.example.squash.technology


class Cartesian {

    companion object {

        private fun flattenList(nestList: List<Any>): List<Any> {
            val flatList = mutableListOf<Any>()

            fun flatten(list: List<Any>) {
                for (e in list) {
                    if (e !is List<*>)
                        flatList.add(e)
                    else
                        @Suppress("UNCHECKED_CAST")
                        flatten(e as List<Any>)
                }
            }

            flatten(nestList)
            return flatList
        }

        private operator fun List<Any>.times(other: List<Any>): List<List<Any>> {
            val prod = mutableListOf<List<Any>>()
            for (e in this) {
                for (f in other) {
                    prod.add(listOf(e, f))
                }
            }
            return prod
        }

        fun nAryCartesianProduct(lists: List<List<Any>>): List<List<Any>> {
            require(lists.size >= 2)
            return lists.drop(2).fold(lists[0] * lists[1]) { cp, ls -> cp * ls }.map { flattenList(it) }
        }


    }


}