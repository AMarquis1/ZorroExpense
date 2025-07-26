package com.marquis.zorroexpense.data.remote.dto

import com.marquis.zorroexpense.domain.model.Category
import com.marquis.zorroexpense.domain.model.Expense
import com.marquis.zorroexpense.domain.model.User

// DTO to Domain mappings
fun UserDto.toDomain(): User = User(
    userId = userId,
    name = name,
    profileImage = profileImage
)

fun CategoryDto.toDomain(): Category = Category(
    name = name,
    icon = icon,
    color = color
)

fun ExpenseDto.toDomain(): Expense = Expense(
    description = description,
    name = name,
    price = price,
    date = date,
    category = category.toDomain(),
    paidBy = paidBy,
    splitWith = splitWith
)

// Domain to DTO mappings
fun User.toDto(): UserDto = UserDto(
    userId = userId,
    name = name,
    profileImage = profileImage
)

fun Category.toDto(): CategoryDto = CategoryDto(
    name = name,
    icon = icon,
    color = color
)

fun Expense.toDto(): ExpenseDto = ExpenseDto(
    description = description,
    name = name,
    price = price,
    date = date,
    category = category.toDto(),
    paidBy = paidBy,
    splitWith = splitWith
)