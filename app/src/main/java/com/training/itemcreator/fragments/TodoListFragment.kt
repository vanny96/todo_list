package com.training.itemcreator.fragments

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.training.itemcreator.R
import com.training.itemcreator.ui.adapters.TodoListRecyclerAdapter
import com.training.itemcreator.model.Todo
import com.training.itemcreator.util.TodoItemTouchHelper
import com.training.itemcreator.util.TodoSort
import com.training.itemcreator.ui.dialogs.AddItemDialogFragment
import com.training.itemcreator.ui.dialogs.TodoFilterDialogFragment
import com.training.itemcreator.ui.snackbars.DeleteTodoSnackbar
import com.training.itemcreator.viewmodel.TodoListViewModel
import com.training.itemcreator.viewmodel.factory.TodoViewModelFactory

class TodoListFragment : Fragment() {

    lateinit var todoListViewModel: TodoListViewModel

    private lateinit var adapter: TodoListRecyclerAdapter
    private lateinit var recyclerView: RecyclerView

    private lateinit var actionButton: FloatingActionButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.todo_list_fragment, container, false)
        setHasOptionsMenu(true)

        initRecycler(view)
        initViewModel(view)

        actionButton = view.findViewById(R.id.floatingActionButton)
        actionButton.setOnClickListener {
            AddItemDialogFragment().show(childFragmentManager, "add_todo_dialog")
        }

        return view;
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.list_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.sort_natural_order -> {
                todoListViewModel.sortOption = TodoSort.ID
                true
            }
            R.id.sort_priority_order -> {
                todoListViewModel.sortOption = TodoSort.PRIORITY
                true
            }
            R.id.filter_action -> {
                TodoFilterDialogFragment().show(childFragmentManager, "filter_fragment")
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun initRecycler(view: View) {
        val onItemClick: (Todo) -> Unit = { todo ->
            todo.id?.let {
                findNavController().navigate(TodoListFragmentDirections.getDetail(todo.name, it))
            }
        }

        val onItemSwipeLeft: (Todo) -> Unit = { todo ->
            DeleteTodoSnackbar(
                view,
                {
                    todo.id?.let { todoListViewModel.deleteItem(it) }
                },
                {
                    todoListViewModel.todoList.value?.let {
                        adapter.data = it
                        adapter.notifyDataSetChanged()
                    }
                }
            ).show()
        }

        adapter = TodoListRecyclerAdapter(
            view.context,
            onItemClick,
            onItemSwipeLeft
        )

        recyclerView = view.findViewById<RecyclerView>(R.id.recycler).apply {
            adapter = this@TodoListFragment.adapter
            layoutManager = LinearLayoutManager(context)
        }

        ItemTouchHelper(TodoItemTouchHelper(adapter)).attachToRecyclerView(recyclerView)
    }

    private fun initViewModel(view: View) {
        todoListViewModel = ViewModelProvider(this, TodoViewModelFactory(view.context))
            .get(TodoListViewModel::class.java)

        todoListViewModel.todoList.observe(viewLifecycleOwner, Observer {
            adapter.data = it
            adapter.notifyDataSetChanged()
        })

        todoListViewModel.todoAdded.observe(viewLifecycleOwner, Observer {
            if (it) {
                Toast.makeText(
                    view.context,
                    getText(R.string.todo_added_toast),
                    Toast.LENGTH_SHORT
                ).show()
                todoListViewModel.switchOffAddedFlag()
            }
        })
    }

    val onAddItem = { todo: Todo ->
        todoListViewModel.addItem(todo)
    }
}
