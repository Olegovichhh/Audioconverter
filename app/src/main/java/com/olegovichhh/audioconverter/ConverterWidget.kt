package com.olegovichhh.audioconverter
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
class ConverterWidget:AppWidgetProvider(){override fun onUpdate(context:Context,manager:AppWidgetManager,ids:IntArray){ids.forEach{id->val intent=Intent(context,MainActivity::class.java).putExtra("pick_file",true);val pending=PendingIntent.getActivity(context,0,intent,PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE);RemoteViews(context.packageName,R.layout.audio_converter_widget).also{it.setOnClickPendingIntent(R.id.widgetOpen,pending);manager.updateAppWidget(id,it)}}}}