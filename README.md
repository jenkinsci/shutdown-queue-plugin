# shutdown-queue

This plugin ensures that short jobs can overtake frozen queue during the shutdown time of long-running tasks.

In shutdown mode, no new jobs are scheduled. But if there is last 24h taking job, and queue is full of short jobs, it is wasted time to not run them.
This plugin, allows  to do exactly this. Set acceptance treshold, set strategy, and no longer waste HW cycles in long shutdown mode. 

Note, that pluginis by default of, and yo need to enable it in configure/Configue system