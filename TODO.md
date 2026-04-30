Download Manager :-
  - Working on :-
    - Trying to get OkHttp requests to cancel

  - Key :-
    - Show progress on the UI
    - Sync download notification with UI

    - Other fixes for the notification

  - Nice To Haves :-
    - Show error message in the UI instead of a generic "Failed"
    - Simultaneous downloading
    - Reordering of queue

  - Testing :-
    - Pausing (UI "works" but download doesn't pause)

  - Bugs :-
    - Some packages REFUSE to download for some reason and fail with an IO FILE EXISTS (or doesn't?) error
    - Notification is persistent even after completion and cancellation (I think, needs testing)
