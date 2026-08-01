package com.voidclient.client.render

import android.content.Context
import android.graphics.Canvas
import android.view.View
import com.voidclient.client.game.ModuleManager
import com.voidclient.client.game.module.visual.BreadcrumbsModule
import com.voidclient.client.game.module.visual.ESPModule
import com.voidclient.client.game.module.visual.StorageESPModule
import com.voidclient.client.game.module.visual.XRayModule

class RenderOverlayView(context: Context) : View(context) {

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        ESPModule.setRenderView(this)
        StorageESPModule.setRenderView(this)
        XRayModule.setRenderView(this)
        BreadcrumbsModule.setRenderView(this)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        ModuleManager.modules
            .filterIsInstance<ESPModule>()
            .filter { it.isEnabled && it.isSessionCreated }
            .forEach { it.render(canvas) }

        ModuleManager.modules
            .filterIsInstance<StorageESPModule>()
            .filter { it.isEnabled && it.isSessionCreated }
            .forEach { it.render(canvas) }

        ModuleManager.modules
            .filterIsInstance<XRayModule>()
            .filter { it.isEnabled && it.isSessionCreated }
            .forEach { it.render(canvas) }

        ModuleManager.modules
            .filterIsInstance<BreadcrumbsModule>()
            .filter { it.isEnabled && it.isSessionCreated }
            .forEach { it.render(canvas) }

        val hasRendering = ModuleManager.modules.any {
            (it is ESPModule || it is StorageESPModule || it is XRayModule || it is BreadcrumbsModule) && it.isEnabled && it.isSessionCreated
        }

        if (hasRendering) {
            postInvalidateOnAnimation()
        }
    }
}