CHRONOPROOF
===========
Distributed Environment Editing Tool
-----------------------------
High Performance P2P Event Logic Processing
-----------------------------
Fork of the **Prova rule language** combined with **Chronicle Engine**
-----------------------

```
for(From,From,From) :- !.
for(From,From,To) :-
        From<=To.
for(I,From,To) :-
        From2=From+1,
        for(I,From2,To).

%%%%%%%%%%%%%%%%% 
% A NOOP worker % 
%%%%%%%%%%%%%%%%% 

rcvMsg(XID,Protocol,From,request,[X|Xs]) :-
println([rcvMsg(XID,Protocol,From,request,[X|Xs])]),
        sendMsg(XID,Protocol,0,reply,[X|Xs]).
```


original readme:
----------------
Prova is an economic and efficient, Java JVM based, open source rule language for reactive agents and event processing. It combines imperative, declarative and functional programming styles. It is designed to work in distributed Enterprise Service Bus and OSGi environments.

The project is led by Alex Kozlenkov (Betfair Ltd., London, England) and Adrian Paschke (Free University, Berlin, Germany)

The issue control is maintained in Prova JIRA over at http://www.prova.ws/jira and the up-to-date documentation is available in Confluence at http://www.prova.ws/confluence.

Latest updates

27 January 2013: Prova 3.2.1 is released. The version is a significant update with new features, enhancements and quality improvements. It includes the new SPARQL operators contributed by the Berlin University team (Malte Rohde) and now updated for OSGi-compatible OpenRDF, faster messaging layer, corrected join operator in the event processing stack, improved packaging of the binary distribution. The Release Notes with the change log for version 3.2.1 are available from the Prova JIRA repository.
