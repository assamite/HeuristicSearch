Heuristic Search
===============

Different heuristic search algorithms implemented in java. Very much work in progress.

**The application is currently developed only with OS X, so some inconsistencies with
the UI (most importantly menubar) may be seen in Linux and Windows.**

Allows the user to load jpg/png images as maps, converts them to grayscale *in memory* 
and uses the resulting grayscale image as the search space for algorithms. Each
pixel is a node and the cost of travelling into that node is depended by the darkness
of the pixel; the darker the more it costs. Only left, right, up, down movements are 
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

References
----------
[A\*, our basic work horse as explained in wikipedia.](https://en.wikipedia.org/wiki/A*)

[Likhachev, Gordon and Thrun: ARA\*: Anytime A\* with Provable Bounds on Sub-Optimality]
(http://machinelearning.wustl.edu/mlpapers/paper_files/NIPS2003_CN03.pdf) Basic ARA\* formulation.


[Koenig and Likhachev: Fast Replanning for Navigation in Unknown Terrain]
(http://pub1.willowgarage.com/~konolige/cs225b/dlite_tro05.pdf) D\* Lite formulation which is built on top of LPA\*.

[Likhachev, *et al*.: Anytime Dynamic A*: An Anytime, Replanning Algorithm]
(https://www.cs.cmu.edu/~maxim/files/ad_icaps05.pdf) - AD\* formulation which builds on top of D\* Lite and ARA\*.

[Likhachev, \*et al\*.: Anytime search in dynamic graphs]
(http://www.cs.helsinki.fi/u/bmmalone/heuristic-search-fall-2013/Likhachev2008.pdf) More thorough formulation of AD\*.

\-\- Simo Linkola // *slinkola (a) cs.helsinki.fi*
