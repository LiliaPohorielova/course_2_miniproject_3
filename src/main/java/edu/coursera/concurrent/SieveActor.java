package edu.coursera.concurrent;

import edu.rice.pcdp.Actor;

import java.util.ArrayList;
import java.util.List;

import static edu.rice.pcdp.PCDP.finish;

public final class SieveActor extends Sieve {

    @Override
    public int countPrimes(final int limit) {
        final SieveActorActor sieveActor = new SieveActorActor(2);
        finish(() -> {
            for (int i = 3; i<= limit; i+=2) {
                sieveActor.send(i);
            }
            sieveActor.send(0);
        });

        int numPrimes = 0;
        SieveActorActor loopActor = sieveActor;
        while (loopActor != null) {
            numPrimes += loopActor.numPrimes;
            loopActor = loopActor.nextActor;
        }

        return numPrimes;
    }

    public static final class SieveActorActor extends Actor {

        private static final int MAX_LOCAL_PRIMES = 500;
        private final List<Integer> primes;
        private int numPrimes;
        private SieveActorActor nextActor;

        public SieveActorActor(int localPrime) {
            primes = new ArrayList<>();
            primes.add(localPrime);
            this.nextActor = null;
            this.numPrimes = 1;
        }

        @Override
        public void process(final Object msg) {
            int integer = (Integer) msg;
            if (integer >= 0) {
                boolean isLocallyPrime = isLocallyPrime(integer);
                if (isLocallyPrime) {
                    if (primes.size() <= MAX_LOCAL_PRIMES) {
                        primes.add(integer);
                        numPrimes++;
                    } else if (nextActor == null) {
                        nextActor = new SieveActorActor(integer);
                    } else {
                        nextActor.send(msg);
                    }
                }
            }
        }

        private boolean isLocallyPrime(final Integer candidate) {
            return primes.stream().noneMatch(prime -> candidate % prime == 0);
        }
    }
}
