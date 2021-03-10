/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.androiddevchallenge

import android.os.Bundle
import android.os.CountDownTimer
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.androiddevchallenge.ui.theme.MyTheme
import kotlin.math.ceil

@ExperimentalAnimationApi
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyTheme {
                MyApp()
            }
        }
    }
}

data class Time(val timeInSec: Int) {
    val min: Int = timeInSec / 60
    val sec: Int = timeInSec - min * 60
}

sealed class TimerState(val time: Time) {
    class STOP(time: Time = Time(0)) : TimerState(time)
    class RUNNING(time: Time) : TimerState(time)
    class PAUSE(time: Time) : TimerState(time)
}

class MainViewModel : ViewModel() {
    private var timer: CountDownTimer? = null

    private val _time = MutableLiveData<TimerState>(TimerState.STOP())
    val time: LiveData<TimerState> = _time

    fun start(timeInSec: Int) {
        _time.value = TimerState.RUNNING(Time(timeInSec))
        timer = createTimer(timeInSec)
            .also { it.start() }
    }

    fun pause(timeInSec: Int) {
        timer?.cancel()
        _time.value = TimerState.PAUSE(Time(timeInSec))
    }

    fun stop() {
        timer?.cancel()
        _time.value = TimerState.STOP()
    }

    private fun createTimer(timeInSec: Int): CountDownTimer {
        return object : CountDownTimer(timeInSec * 1000L, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val t = ceil(millisUntilFinished / 1000f).toInt()
                _time.value = TimerState.RUNNING(Time(t))
            }

            override fun onFinish() {
                _time.value = TimerState.STOP()
            }
        }
    }

    fun onNumberClicked(num: Int) {
        val time = time.value!!.time
        if (time.min > 9) {
            // ignore
            return
        }
        val newMin0 = time.min
        val newMin1 = time.sec / 10
        val newSec0 = time.sec % 10
        val newSec1 = num
        val newTime = (newMin0 * 10 + newMin1) * 60 + (newSec0 * 10) + newSec1
        _time.value = TimerState.STOP(Time(newTime))
    }

    fun onDeleteClicked() {
        val time = time.value!!.time
        val newMin0 = 0
        val newMin1 = time.min / 10
        val newSec0 = time.min % 10
        val newSec1 = time.sec / 10
        val newTime = (newMin0 * 10 + newMin1) * 60 + (newSec0 * 10) + newSec1
        _time.value = TimerState.STOP(Time(newTime))
    }
}

// Start building your app here!
@ExperimentalAnimationApi
@Composable
fun MyApp(mainViewModel: MainViewModel = viewModel()) {

    val timerState by mainViewModel.time.observeAsState(TimerState.STOP())

    Surface(color = MaterialTheme.colors.background) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
        ) {
            Row {
                val time = timerState.time
                Text(
                    text = String.format("%02d:%02d", time.min, time.sec),
                    fontSize = 60.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                )
            }
            when (timerState) {
                is TimerState.STOP -> {
                    Button(
                        enabled = timerState.time.timeInSec > 0,
                        onClick = {
                            mainViewModel.start(timerState.time.timeInSec)
                        }
                    ) {
                        Image(
                            painterResource(id = R.drawable.ic_play),
                            contentDescription = "start"
                        )
                    }
                }
                is TimerState.RUNNING -> {
                    Button(
                        onClick = {
                            mainViewModel.pause(timerState.time.timeInSec)
                        }
                    ) {
                        Image(
                            painterResource(id = R.drawable.ic_pause),
                            contentDescription = "pause"
                        )
                    }
                }
                is TimerState.PAUSE -> {
                    Row {
                        Button(
                            onClick = {
                                mainViewModel.start(timerState.time.timeInSec)
                            }
                        ) {
                            Image(
                                painterResource(id = R.drawable.ic_play),
                                contentDescription = "resume"
                            )
                        }
                        Spacer(modifier = Modifier.size(16.dp))
                        Button(
                            onClick = {
                                mainViewModel.stop()
                            }
                        ) {
                            Image(
                                painterResource(id = R.drawable.ic_stop),
                                contentDescription = "stop"
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(48.dp))
            AnimatedVisibility(visible = timerState is TimerState.STOP) {
                NumPad(
                    onNumberClicked = mainViewModel::onNumberClicked,
                    onDeleteClicked = mainViewModel::onDeleteClicked
                )
            }
        }
    }
}

@Composable
fun NumPad(onNumberClicked: (Int) -> Unit, onDeleteClicked: () -> Unit) {
    val p = remember { Modifier.padding(4.dp) }
    Column {
        Row {
            Button(onClick = { onNumberClicked(1) }, p) {
                Text("1")
            }
            Button(onClick = { onNumberClicked(2) }, p) {
                Text("2")
            }
            Button(onClick = { onNumberClicked(3) }, p) {
                Text("3")
            }
        }
        Row {
            Button(onClick = { onNumberClicked(4) }, p) {
                Text("4")
            }
            Button(onClick = { onNumberClicked(5) }, p) {
                Text("5")
            }
            Button(onClick = { onNumberClicked(6) }, p) {
                Text("6")
            }
        }
        Row {
            Button(onClick = { onNumberClicked(7) }, p) {
                Text("7")
            }
            Button(onClick = { onNumberClicked(8) }, p) {
                Text("8")
            }
            Button(onClick = { onNumberClicked(9) }, p) {
                Text("9")
            }
        }
        Row {
            Button(onClick = { onNumberClicked(0) }, p) {
                Text("0")
            }
            Button(onClick = { onDeleteClicked() }, p) {
                Text("Del")
            }
        }
    }
}

@ExperimentalAnimationApi
@Preview("Light Theme", widthDp = 360, heightDp = 640)
@Composable
fun LightPreview() {
    MyTheme {
        MyApp()
    }
}

@ExperimentalAnimationApi
@Preview("Dark Theme", widthDp = 360, heightDp = 640)
@Composable
fun DarkPreview() {
    MyTheme(darkTheme = true) {
        MyApp()
    }
}
