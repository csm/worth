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