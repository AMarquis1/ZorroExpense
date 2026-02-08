package com.marquis.zorroexpense.presentation.state

import com.marquis.zorroexpense.domain.model.Category
import com.marquis.zorroexpense.domain.model.ExpenseList
import com.marquis.zorroexpense.domain.model.User

/**
 * Screen mode for ExpenseListDetailScreen
 */
enum class ListDetailMode {
    /** View-only mode - displays list details */
    VIEW,

    /** Edit mode - allows modifying existing list */
    EDIT,

    /** Add mode - creating a new list */
    ADD,
}

sealed class ExpenseListDetailUiState {
    data object Loading : ExpenseListDetailUiState()

    data class Success(
        val expenseList: ExpenseList,
        val mode: ListDetailMode = ListDetailMode.VIEW,
        val showDeleteDialog: Boolean = false,
        val showDeleteMemberDialog: Boolean = false,
        val memberToDelete: User? = null,
        val isSaving: Boolean = false,
        val showCategoryBottomSheet: Boolean = false,
        // Editable fields for EDIT/ADD modes
        val editedName: String = expenseList.name,
        val editedCategories: List<Category> = expenseList.categories,
        val editedMembers: List<User> = expenseList.members,
    ) : ExpenseListDetailUiState()

    data object Deleted : ExpenseListDetailUiState()

    data class Error(
        val message: String,
    ) : ExpenseListDetailUiState()
}

sealed class ExpenseListDetailUiEvent {
    data object DeleteList : ExpenseListDetailUiEvent()

    data object ConfirmDelete : ExpenseListDetailUiEvent()

    data object CancelDelete : ExpenseListDetailUiEvent()

    /** Switch to edit mode */
    data object EnterEditMode : ExpenseListDetailUiEvent()

    /** Cancel edit and return to view mode */
    data object CancelEdit : ExpenseListDetailUiEvent()

    /** Save changes (for EDIT/ADD modes) */
    data object SaveChanges : ExpenseListDetailUiEvent()

    /** Update list name */
    data class UpdateName(val name: String) : ExpenseListDetailUiEvent()

    /** Add a category */
    data object AddCategoryClicked : ExpenseListDetailUiEvent()

    /** Toggle a category selection in the bottom sheet */
    data class CategoryToggled(val category: Category) : ExpenseListDetailUiEvent()

    /** Dismiss the category selection bottom sheet */
    data object DismissCategoryBottomSheet : ExpenseListDetailUiEvent()

    /** Remove a category */
    data class RemoveCategory(val category: Category) : ExpenseListDetailUiEvent()

    /** Remove a member */
    data class RemoveMember(val member: User) : ExpenseListDetailUiEvent()

    /** Confirm member deletion */
    data object ConfirmDeleteMember : ExpenseListDetailUiEvent()

    /** Cancel member deletion */
    data object CancelDeleteMember : ExpenseListDetailUiEvent()
}
