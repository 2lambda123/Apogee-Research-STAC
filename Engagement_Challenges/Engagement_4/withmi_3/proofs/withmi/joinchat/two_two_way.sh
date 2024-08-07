#!/usr/bin/expect
set timeout -1

spawn ../../../challenge_program/bin/withmi -d ../../../challenge_program/data -s ./sally -i ../../../examples/sally.id
set sally_pid $spawn_id

spawn ../../../challenge_program/bin/withmi -d ../../../challenge_program/data -s ./megan -i ../../../examples/megan.id
set megan_pid $spawn_id

spawn ../../../challenge_program/bin/withmi -d ../../../challenge_program/data -s ./deven -i ../../../examples/deven.id
set deven_pid $spawn_id

# sally sets up her two chats #########################

expect -i $sally_pid {WithMi>}
send -i $sally_pid "\n"
expect -i $sally_pid "WithMi>"

# connect sally to megan
send -i $sally_pid "connect 127.0.0.1 9002\n"
expect -i $sally_pid {megan. callback on: localhost:9002}

# connect sally to deven
send -i $sally_pid "connect 127.0.0.1 9001\n"
expect -i $sally_pid {deven. callback on: localhost:9001}

# create sally megan chat
send -i $sally_pid "createchat chatMS\n"
expect -i $sally_pid {Successfully created chatMS}

# create sally devan chat
send -i $sally_pid "createchat chatSD\n"
expect -i $sally_pid {Successfully created chatSD}

# sally adds users to her chats
send -i $sally_pid "joinchat chatMS\n"
expect -i $sally_pid "joined chatMS"

send -i $sally_pid "adduser megan\n"
expect -i $sally_pid "Added user to group"

send -i $sally_pid "joinchat chatSD\n"
expect -i $sally_pid "joined chatSD"
send -i $sally_pid "adduser deven\n"
expect -i $sally_pid "Added user to group"

# sally sends messages to her folks
send -i $sally_pid "Hey what's up?\n"
expect -i $deven_pid "*sally: Hey what's up?"
send -i $sally_pid "joinchat chatMS\n"
expect -i $sally_pid "joined chatMS"
send -i $sally_pid "Hey what's up?\n"
expect -i $megan_pid "*sally: Hey what's up?"

# exit
send -i $sally_pid "exit\n"
expect -i $sally_pid eof

send -i $megan_pid "exit\n"
expect -i $megan_pid eof

send -i $deven_pid "exit\n"
expect -i $deven_pid eof

