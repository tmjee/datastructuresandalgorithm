/*
 * Copyright (c) 2014, Oracle America, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 *  * Neither the name of Oracle nor the names of its contributors may be used
 *    to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.sample;

import conc.Jdk7ConcurrentSkipListMap;
import conc.Jdk7ConcurrentSkipListSet;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class ConcurrentSkipListSetBenchmark {

   enum Cat {
       FIRST_1,
       FIRST_2,
       SECOND,
       THIRD
   }

    @State(Scope.Thread)
    public static class Randomness {
        Cat c;
        @Setup(Level.Invocation)
        public void setup() {
            int i = ThreadLocalRandom.current().nextInt(0, 100);
            if (i <= 15) {
                c = Cat.FIRST_1;
            } else if (i <= 30) {
                c = Cat.FIRST_2;
            } else if (i <=40) {
                c= Cat.SECOND;
            } else {
                c= Cat.THIRD;
            }
        }
    }


    @State(Scope.Benchmark)
    public static class BenchmarkState1  {
        volatile ConcurrentSkipListSet<Integer> c;

        @Setup()
        public void setup() {
            c = new ConcurrentSkipListSet<Integer>();
        }
    }

    @State(Scope.Benchmark)
    public static class BenchmarkState2 {
        volatile conc.ConcurrentSkipListSet<Integer> c;

        @Setup
        public void setup() {
            c = new conc.ConcurrentSkipListSet<Integer>();
        }
    }

    @State(Scope.Benchmark)
    public static class BenchmarkState3 {
        volatile Jdk7ConcurrentSkipListSet<Integer> c;

        @Setup
        public void setup() {
            c = new Jdk7ConcurrentSkipListSet<Integer>();
        }
    }

    @Benchmark
    @BenchmarkMode({Mode.Throughput, Mode.SampleTime})
    @OutputTimeUnit(TimeUnit.SECONDS)
    public void baseline(Randomness r, Blackhole bh) {
        switch(r.c) {
            case FIRST_1:
                bh.consume(ThreadLocalRandom.current().nextInt());
                break;
            case FIRST_2:
                bh.consume(ThreadLocalRandom.current().nextInt());
                break;
            case SECOND:
                bh.consume(new Object());
                break;
            case THIRD:
                bh.consume(ThreadLocalRandom.current().nextInt());
                break;
        }
    }

    @Benchmark
    @BenchmarkMode({Mode.Throughput, Mode.SampleTime})
    @OutputTimeUnit(TimeUnit.SECONDS)
    public void jcuConcurrentSkipListSet(Randomness r, BenchmarkState1 s, Blackhole bh) {
        switch(r.c) {
            case FIRST_1:
                bh.consume(s.c.add(ThreadLocalRandom.current().nextInt()));
                break;
            case FIRST_2:
                bh.consume(s.c.remove(ThreadLocalRandom.current().nextInt()));
                break;
            case SECOND:
                bh.consume(s.c.size());
                break;
            case THIRD:
                bh.consume(s.c.contains(ThreadLocalRandom.current().nextInt()));
                break;
        }
    }


    @Benchmark
    @BenchmarkMode({Mode.Throughput, Mode.SampleTime})
    @OutputTimeUnit(TimeUnit.SECONDS)
    public void concConcurrentSkipListSet(Randomness r, BenchmarkState2 s, Blackhole bh) {
        switch(r.c) {
            case FIRST_1:
                bh.consume(s.c.add(ThreadLocalRandom.current().nextInt()));
                break;
            case FIRST_2:
                bh.consume(s.c.remove(ThreadLocalRandom.current().nextInt()));
                break;
            case SECOND:
                bh.consume(s.c.size());
                break;
            case THIRD:
                bh.consume(s.c.contains(ThreadLocalRandom.current().nextInt()));
                break;
        }
    }

    @Benchmark
    @BenchmarkMode({Mode.Throughput, Mode.SampleTime})
    @OutputTimeUnit(TimeUnit.SECONDS)
    public void jdk7ConcurrentSkipListSet(Randomness r, BenchmarkState3 s, Blackhole bh) {
        switch(r.c) {
            case FIRST_1:
                bh.consume(s.c.add(ThreadLocalRandom.current().nextInt()));
                break;
            case FIRST_2:
                bh.consume(s.c.remove(ThreadLocalRandom.current().nextInt()));
                break;
            case SECOND:
                bh.consume(s.c.size());
                break;
            case THIRD:
                bh.consume(s.c.contains(ThreadLocalRandom.current().nextInt()));
                break;
        }
    }


    public static void main(String[] args) throws Exception {
        Options opt = new OptionsBuilder()
                .include(ConcurrentSkipListSetBenchmark.class.getSimpleName())
                .warmupIterations(5)
                .measurementIterations(5)
                .forks(1)
                .threads(4)
                .build();
        new Runner(opt).run();
    }

}
