
/*-
 * ============LICENSE_START==========================================
 * OPENECOMP - DCAE
 * ===================================================================
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 * ===================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0 
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END============================================
 */
	
package org.openecomp.logger;

import static com.att.eelf.configuration.Configuration.MDC_BEGIN_TIMESTAMP;
import static com.att.eelf.configuration.Configuration.MDC_ELAPSED_TIME;
import static com.att.eelf.configuration.Configuration.MDC_END_TIMESTAMP;
import static com.att.eelf.configuration.Configuration.MDC_TARGET_ENTITY;
import static com.att.eelf.configuration.Configuration.MDC_TARGET_SERVICE_NAME;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.slf4j.MDC;


/**
 * This class is used to time events to determine their duration. The stop watch allows for the same types of operations
 * as a real stop watch, that is, it allows the timing to be stopped, started, cleared, and read. The accumulated time
 * is the total of all times between start and stop events. The watch can be repeatedly stopped and restarted, and will
 * accumulate all durations between start/stop pairs.
 */
public class Stopwatch {
	
	public static String ISO_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    public static final TimeZone utc = TimeZone.getTimeZone("UTC");
    public static final SimpleDateFormat isoFormatter = new SimpleDateFormat(ISO_FORMAT);
    public static EcompLogger ecompLogger = EcompLogger.getEcompLogger();

    /**
     * This is the object that maintains our state on the thread local storage
     */
    public static class StopwatchState {
        /**
         * The accumulated duration
         */
        private long duration;

        /**
         * Indicates that the watch is running
         */
        private boolean running = false;

        /**
         * The last recorded start time
         */
        private long startTime;

		public String target;

		public String op;

		@Override
		public String toString() {
			return "StopwatchState [duration=" + duration + ", running=" + running + ", startTime=" + startTime + "]";
		}

    }
    
    public static class StopwatchStateStack {
        private List<StopwatchState> l = new ArrayList<Stopwatch.StopwatchState>();

		public StopwatchState top() {
//			System.out.println("WWWWWW: " + Thread.currentThread().getName() + ": top 1 " + l);
			if (l.size() == 0) {
				ecompLogger.warn(GenericMessagesMessageEnum.ECOMP_LOGGER_TOP_ON_EMPTY_STACK);
//				System.out.println("WWWWWW: " + Thread.currentThread().getName() + ": top empty");
				push(null,null);
			}
//			System.out.println("WWWWWW: " + Thread.currentThread().getName() + ": top 2 " + l);
			return l.get(l.size()-1);
		}

		public void push(String targetEntity, String target) {
			l.add(new StopwatchState());
			top().op = targetEntity;
			top().target = target;
//			System.out.println("WWWWWW: " + Thread.currentThread().getName() + ": push 2 " + l);
		}

		public void pop() {
//			System.out.println("WWWWWW: " + Thread.currentThread().getName() + ": pop 1 " + l);
			if (l.size() == 0) {
				ecompLogger.warn(GenericMessagesMessageEnum.ECOMP_LOGGER_POP_ON_EMPTY_STACK);
				return;
			}
			l.remove(l.size()-1);
		}

		public void clear() {
//			System.out.println("WWWWWW: " + Thread.currentThread().getName() + ": clear 1" + l);
			l.clear();
		}
    }

    /**
     * Thread local storage wrapper
     */
    private static ThreadLocal<StopwatchStateStack> tls = new ThreadLocal<>();
    
    static {
        isoFormatter.setTimeZone(utc);
    }

    /**
     * Looks up the Thread Local storage object containing the Stopwatch state, and creates it if it does not already
     * exist.
     * 
     * @return The state object
     */
    private static StopwatchStateStack getState() {
        StopwatchStateStack state = tls.get();
        if (state == null) {
            state = new StopwatchStateStack();
            tls.set(state);
        }
        return state;
    }

    /**
     * Clears (and possibly stops) the watch.
     */
    public static void clear() {
        getState().clear();
    }

    /**
     * The accumulated duration of the watch (in nano-seconds)
     * 
     * @return The accumulated time
     */
    public static long getDuration() {
        StopwatchState state = getState().top();
        return state.duration;
    }

    /**
     * Determines if the stopwatch is currently running or not
     * 
     * @return True if the watch is running
     */
    public static boolean isRunning() {
        StopwatchState state = getState().top();
        return state.running;
    }

    /**
     * Starts the watch if not already running.
     */
    public static void start() {
        StopwatchState state = getState().top();
        if (!state.running) {
            state.running = true;
            state.startTime = System.currentTimeMillis();
            MDC.put(MDC_BEGIN_TIMESTAMP, isoFormatter.format(new Date(state.startTime)));
        }
    }

    /**
     * Stops the accumulation of time on the watch if running
     */
    public static void stop() {
        StopwatchState state = getState().top();
        if (state.running) {
            long stopTime = System.currentTimeMillis();
            state.duration += (stopTime - state.startTime);
            state.running = false;
            MDC.put(MDC_END_TIMESTAMP,isoFormatter.format(new Date(stopTime)));
            MDC.put(MDC_ELAPSED_TIME, String.valueOf(state.duration));
        }
		if (!EcompLogger.isNullOrEmpty(state.target))
			MDC.put(MDC_TARGET_ENTITY, state.target);
		if (state.op != null)
			MDC.put(MDC_TARGET_SERVICE_NAME, state.op);
    }

    /**
     * Gets the amount of time since the stop watch was last started without stopping the watch or accumulating the
     * previous time .
     */
    public static double getCurrentDuration() {
        StopwatchState state = getState().top();
        if (state.running) {
            return (System.currentTimeMillis() - state.startTime);
        }
        return 0L;
    }
    
    public static void pushNewWatch(String targetEntity, String target) {
//    	System.out.println("WWWWWW: " + Thread.currentThread().getName() + ": pushNewWatch 1" + getState().l);
    	getState().push(targetEntity,target);
//    	System.out.println("WWWWWW: " + Thread.currentThread().getName() + ": pushNewWatch 2" + getState().l);
    }
    
    public static void popWatch() {
    	stop();
    	getState().pop();
    }

	public static void clearAndStart() {
//		System.out.println("WWWWWW: " + Thread.currentThread().getName() + " clearAndStart 1" + getState().l);
		clear();
//		System.out.println("WWWWWW: " + Thread.currentThread().getName() + ": clearAndStart 2" + getState().l);
		pushAndStart(null,null);
//		System.out.println("WWWWWW: " + Thread.currentThread().getName() + ": clearAndStart 3" + getState().l);
	}

	public static void stopAndPop() {
//		System.out.println("WWWWWW: " + Thread.currentThread().getName() + ": stopAndPop 1" + getState().l);
		stop();
//		System.out.println("WWWWWW: " + Thread.currentThread().getName() + ": stopAndPop 2" + getState().l);
		popWatch();
//		System.out.println("WWWWWW: " + Thread.currentThread().getName() + ": stopAndPop 3" + getState().l);
	}

	public static void pushAndStart(String targetEntity, String target) {
//		System.out.println("WWWWWW: " + Thread.currentThread().getName() + ": pushAndStart 1" + getState().l);
		pushNewWatch(targetEntity,target);
//		System.out.println("WWWWWW: " + Thread.currentThread().getName() + ": pushAndStart 2" + getState().l);
		start();
//		System.out.println("WWWWWW: " + Thread.currentThread().getName() + ": pushAndStart 3" + getState().l);
	}

	public static boolean emptyStack() {
		return getState().l.size() == 0;
	}

	public static String getTopTarget() {
		return getState().top().target;
	}
}
