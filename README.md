# worth

Inspired by https://www.jwz.org/hacks/worth.pl.

Usage:

    lein run -- (options...)

      -t, --ticker TICKER                 Stock ticker symbol.
      -u, --units UNITS                   Number of stock units or options.
      -p, --strike PRICE           0.0    Set strike price.
      -s, --sold COUNT             0      Number of shares already sold.
      -b, --start-date YYYY-MM-DD         Vesting schedule start date.
      -e, --end-date YYYY-MM-DD           Vesting schedule end date.
      -r, --rate RATE              month  Maturation rate.
      -h, --help                          Show this help and exit.

Caveats: assumes shares all vest at the same rate, and don't have a "cliff". Your mileage may vary.

Example (no I don't work for Google):

    $ lein run -- --ticker GOOG --units 10 --start-date 2013-09-21 --end-date 2017-09-21 --rate month --strike 300
    Today's GOOG price is $642.61; your total unsold shares are worth $3,426.10.
    You are 52% vested, for a total of 5 vested unsold shares ($1,784.43).
    But if you quit today, you will walk away from $1,641.67.
    Hang in there little trooper!  Only 1 year, 10 months, 2 days left!