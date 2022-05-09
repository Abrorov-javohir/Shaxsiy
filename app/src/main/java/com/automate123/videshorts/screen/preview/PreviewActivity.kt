package com.automate123.videshorts.screen.preview

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.automate123.videshorts.R
import com.automate123.videshorts.databinding.ActivityPreviewBinding
import com.automate123.videshorts.screen.BaseActivity

class PreviewActivity : BaseActivity() {

    private lateinit var binding: ActivityPreviewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_preview, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_share -> {
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}