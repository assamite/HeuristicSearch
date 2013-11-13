Heuristic Search
===============

Different heuristic search algorithms implemented in java. Very much work in progress.

**The application is currently developed only with OS X, so some inconsistencies with
the UI (most importantly menubar) may be seen in Linux and Windows.**

Allows the user to load jpg/png images as maps, converts them to grayscale *in memory* 
and uses the resulting grayscale image as the search space for algorithms. Each
pixel is a node and the cost of travelling into that node is depended by the darkness
of the pixel; the darker the more it costs. Only left, rigt, up, down movements are 
currently allowed.

Currently application's main resides in src/ui/UILauncher. 

See Help-menu for Instructions of how to setup root and goal states, etc..

Current Implementation Status
--------------
| Algorithm | %          | status      | other comments                     |
| --------- | ---------- | ----------- | -----------------------------------| 
| A*        | ********** | DONE        |                                    |
| D* Lite   | ******     | started     | some peculiar bug with replanning  | 
| ARA*      | ******     | started     |                                    |
| AD*       |            | not started |                                    |

Some other algorithms might be implemented at *some* point in the (very distant) future (in the galaxy very far away).


- Simo Linkola // slinkola (a) cs.helsinki.fi
