package org.jetbrains.dataframe

import org.jetbrains.dataframe.api.columns.ColumnData

operator fun <T> DataFrame<T>.plus(col: DataCol) = dataFrameOf(columns + col).typed<T>()

operator fun <T> DataFrame<T>.plus(col: Iterable<DataCol>) = dataFrameOf(columns + col).typed<T>()

fun <T> DataFrame<T>.add(name: String, data: DataCol) = dataFrameOf(columns + data.doRename(name)).typed<T>()

inline fun <reified R, T> DataFrame<T>.add(name: String, noinline expression: RowSelector<T, R>) =
        (this + newColumn(name, expression))

inline fun <reified R, T, G> GroupedDataFrame<T, G>.add(name: String, noinline expression: RowSelector<G, R>) =
        updateGroups { add(name, expression) }

inline fun <reified R, T> DataFrame<T>.add(column: ColumnDefinition<R>, noinline expression: RowSelector<T, R>) =
        (this + newColumn(column.name(), expression))

fun <T> DataFrame<T>.add(body: TypedColumnsFromDataRowBuilder<T>.() -> Unit) =
        with(TypedColumnsFromDataRowBuilder(this)) {
            body(this)
            dataFrameOf(this@add.columns + columns).typed<T>()
        }

operator fun <T> DataFrame<T>.plus(body: TypedColumnsFromDataRowBuilder<T>.() -> Unit) = add(body)

class TypedColumnsFromDataRowBuilder<T>(val df: DataFrame<T>): DataFrameBase<T> by df {
    internal val columns = mutableListOf<DataCol>()

    fun add(column: DataCol) = columns.add(column)

    inline fun <reified R> add(name: String, noinline expression: RowSelector<T, R>) = add(df.newColumn(name, expression))

    inline fun <reified R> add(columnDef: ColumnDef<R>, noinline expression: RowSelector<T, R>) = add(df.newColumn(columnDef.name(), expression))

    inline operator fun <reified R> ColumnDef<R>.invoke(noinline expression: RowSelector<T, R>) = add(df.newColumn(name(), expression))

    inline operator fun <reified R> String.invoke(noinline expression: RowSelector<T, R>) = add(this, expression)

    operator fun String.invoke(column: DataCol) = add(column.doRename(this))

    inline operator fun <reified R> ColumnDef<R>.invoke(column: ColumnData<R>) = name()(column)

    infix fun DataCol.into(name: String) = add(doRename(name))
}