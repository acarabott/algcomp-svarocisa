Computational Music 1
Task 2 - Algorithmic composition "Svarocisa"

//SynthDefs
(

SynthDef(\newOne) { |freq|
	var sig = SinOsc.ar(freq, 0, 0.1);
	Out.ar(0,
		sig;	
	);
}.load(s);

//Drone
(
SynthDef(\drone, {arg rootNote=59;

var signal1, signal2, root, fifth, octaveA, octaveB, env;
root = rootNote.midicps;
fifth = (rootNote+7).midicps;
octaveA = (rootNote+12).midicps;
octaveB = (rootNote-12).midicps;

env = {EnvGen.kr(Env.new(
				 					Array.rand(16, 0, 0.2),  //Random drones
									Array.rand(15, 1, 5),
									'exponential',
									0,
									1))};
signal1 = Mix(SinOsc.ar([root, fifth, [octaveA, octaveB].choose], 0, 0.3*[env, env, env]));
signal2 = Mix(LFSaw.ar([root, fifth, [octaveA, octaveB].choose], 0, 0.4*[env, env, env]));							

Out.ar(	0,
 		Pan2.ar(signal1),
 		Pan2.ar(signal2, FSinOsc.kr(0.05))
 		);
}).send(s)
);

//Kanjira
(
SynthDef("kanjira",{ arg bufnum=0, startPos=0, dur, amp=1, pan=0.0;
var signal;

signal = PlayBuf.ar(1,
								bufnum, 										//Buffer to play from
								BufRateScale.kr(bufnum)*1, 					//Scale buffer rate to ensure playback at correct rate
								0, 
								startPos, 										//Position in the buffer to play from
								0)
								*EnvGen.kr(Env.new(	[0,1,1,0], 		
														[0.001,dur/1.666,0.001]
														), 
											doneAction:2);					//Envelope to ensure correct duration and to avoid clicks when cutting
								
	Out.ar(0,
		FreeVerb.ar( 	Pan2.ar(signal*amp, pan),
						0.25,
						0.75,
						0.25))
}).store; 
);

//Guitar
(
SynthDef("guitar",{ arg bufnum=0, startPos=0, dur, amp=1, pan=0.5;
var signal;
signal = PlayBuf.ar(1,
								bufnum, 										//Buffer to play from
								BufRateScale.kr(bufnum)*1, 					//Scale buffer rate to ensure playback at correct rate
								0, 
								startPos, 										//Position in the buffer to play from
								0)
								*EnvGen.kr(Env.new(	[0,1,1,0], 
														[0.001,dur/1.666,0.001]
														), 
											doneAction:2);	//Envelope to ensure correct duration and to avoid clicks when cutting
								
	Out.ar(0,
		FreeVerb.ar( 	Pan2.ar(signal*amp, pan),
						0.25,
						0.75,
						0.25))
}).store; 
);

)

//Algorithmic composition
(
var 

rf,																	//Rest Function
r = Array.newClear(4),											//Rests
dronePat,															//Drone Pattern

kb, 																//Kanjira Buffer
kbh,																//Kanjira Buffer frames per beat (total frames / 7)
kf,																	//Kanjira Function
kif,																//Kanjira Improvisation Function
kdf,																//Kanjira Dynamics Function
kh = Array.newClear(15), 										//Kanjira Hits
ksq,																//Kanjira semiquaver hits
kdsq,																//Kanjira demisemiquaver hits
kc = Array.newClear(15),											//Kanjira Cells
ki,																	//Kanjira improvisation array
kp = Array.newClear(15),											//Kanjira Patterns
ks, ks2,															//Kanjira Solos

gb = Array.newClear(15),											//Guitar Buffers
gf,																	//Guitar Function
gif7,																//Guitar Improvisation Function (7/16 length samples)
gif6,																//Guitar Improvsation Function 

gc = Array.newClear(25),											//Guitar Cells
gp = Array.newClear(20),											//Guitar Patterns
gs1, gs2,															//Guitar Solos
bc1, bc2, bc3, bc4, bc5, bc6, bc7, bc8, bc9, bc10,			//Both Cells
bp1, bp2, bp3, bp4, bp5, bp6, bp7, bp8, bp9, bp10,			//Both Patterns

hSoloRep,
intro, mainTheme, improvTheme, altTheme, preSolo, humanSolo, cyborgSolo, robotSolo, ending;		//sections

{																	//Start of 'routine' so that the server can be sync'ed, meaning buffer lengths are calculated and can be accessed. 
//Drone Pattern

dronePat = Pbind(
	[\midinote, \dur, \instrument],
	[59, 500, \drone]
	);
	
//Kanjira Buffer	
kb	= Buffer.read(s, "sounds/kanjira78.wav");
s.sync; 
kbh = kb.numFrames/7;

//Kanjira Functions

	//Standard function to play a duration of the Kanjira sample
kf = { arg start = 0, xdur=0.5, xamp=1; Pbind([\instrument, \startPos, \dur, \amp, \bufnum, \pan ], Pseq([[\kanjira, start, xdur, xamp, kb.bufnum, (-0.8,-0.7..-0.3).choose]]) )};
	
	//Kanjira improvisation function. Improvises by starting at random hits from the sample, using a given array of durations, for a given length of time.
kif = { arg length, xdurs; Psync(
									Pbind(\instrument, 	\kanjira, 
											\startPos, 	Prand([0].addAll((1..6)*kbh), inf), 	//Start at a random beat of the buffer
											\dur, 			Prand((xdurs),inf), 						//Random duration from an array 
											\amp, 			Prand((0.4,0.5..1),inf), 				//Random amplitude between 0.4 and 1
											\bufnum, 		kb.bufnum, 								//Buffer number of the Kanjira buffer
											\pan,			Prand((-1.0,-0.9..1.0),inf)				//Random Pan value between -1.0 (100% Left) and 1.0 (100% Right)
											), 
									length, length													//Length of the phrase
									)};
	
	//Kanjira dynamics function											
kdf ={ (0.4,0.5..1).wchoose((1..7).normalizeSum)};			//Weighted random value between 0.4 and 1 (greater amplitude is more likely)

//Rests
r = {arg dur; kf.value(0, dur, 0)};		

//Kanjira Hits
kh[0] = kf.value(0, 0.5, kdf, 0);					//Dom 	1/8
kh[1] = kf.value(0, 0.25, kdf, 0);					//Dom 	1/16
kh[2] = kf.value(kbh*2, 0.25, kdf);				//Da	1/16
kh[3] = kf.value(kbh*2, 0.125, kdf);				//Da	1/32
kh[4] = kf.value(kbh*3, 0.25, kdf);				//Ga	1/16		
kh[5] = kf.value(kbh*3, 0.125, kdf);				//Ga	1/32
kh[6] = kf.value(kbh*4, 0.25, kdf);				//Di	1/16
kh[7] = kf.value(kbh*4, 0.125, kdf);				//Di	1/32
kh[8] = kf.value(kbh*5, 0.25, kdf);				//Gi	1/16
kh[9] = kf.value(kbh*5, 0.125, kdf);				//Gi	1/32
kh[10] = kf.value(kbh*6, 0.25, kdf);				//Dum	1/16
kh[11] = kf.value(kbh*6, 0.125, kdf);				//Dum	1/32

//Kanjira Hits Arrays
ksq = [kh[1], kh[2], kh[4], kh[6], kh[8], kh[10]];		//All 1/16th Kanjira Hits
kdsq = [kh[3], kh[5], kh[7], kh[9], kh[11]];				//All 1/32nd Kanjira Hits

//Kanjira Cells
kc[0] = kf.value(kbh*2, 0.5, kdf);															//Ta-ka			1/16ths
kc[1] = kf.value(kbh*3, 0.5, kdf);															//Ta-ka			1/16ths
kc[2] = kf.value(kbh*4, 0.5, kdf);															//Ta-ka			1/16ths
kc[3] = kf.value(kbh*5, 0.5, kdf);															//Ta-ka			1/16ths
kc[4] = Pxrand(kdsq, 2);																		//Ta-ka			1/32nds
kc[5] = Pxrand(kdsq, 4);																		//Ta-ka-di-mi	1/32nds
kc[6] = Pxrand(ksq, 3);																		//Ta-ki-ta		1/16ths
kc[7] = Pseq([Pxrand(kdsq, 3), r.value(0.125)], 1);										//Ta-ki-ta-rest	1/32nds
kc[8] = Pseq([Prand(ksq, 1), kc[4]], 1);													//Ta-(ka)-di-mi	1/32nds
kc[9] = Pseq([kc[4], r.value(0.125), Prand(kdsq, 1)], 1);								//Ta-ka-(di)-mi	1/32nds
kc[10] = [kc[0], kc[1], kc[2], kc[3], kc[4], kc[5], kc[6], kc[7], kc[8], kc[9]];		//All cells, necessary as just using kc doesn't work for some reason  

//Collection of Kanjira hits/cells/rests for improvisation
ki = [kh[0], r.value(0.25), r.value(0.5)].addAll(ksq.addAll(kc[10]));			
	
//Kanjira Patterns	
kp[0] = kf.value(0, 1.75, kdf);																							//Standard 7/16 Pattern
kp[1] = {arg reps; Pseq([kh[0], Psync(Prand(ki, inf), 1.25, 1.25)], reps)};										//Improvised 7/16 Pattern
kp[2] = Pseq([kh[1], Prand([kc[0], kc[1], kc[2], kc[3]], 1)], 2);													//Standard 6/16 Pattern
kp[3] = Pseq([kh[1], Psync(Prand(ki, inf), 0.5, 0.5)], 2);															//Improvised 6/16 Pattern
kp[4] = Pseq([Pseq([kp[0]],3), kp[2]],1);																				//Main 7776 riff
kp[5] = Pseq([ kh[0], kh[0], Pxrand([kc[0],kc[1],kc[2], kc[3]],4), kh[0], kh[0]],1);							//Human solo introduction
kp[6] = {arg dur; Pseq([ kh[0], r.value(dur-0.5)],1)};																//Solo accomp 1 hit
kp[7] = {arg dur; Pseq([ kh[1], kh[1], r.value(dur-0.5)],1)};														//Solo accomp 2 hits
kp[8] = {arg dur1, length; Psync(Prand([ kp[6].value(dur1), kp[7].value(dur1)], inf), length, length)};		//Random Solo accomps
kp[9] = Pseq([ kh[0], kh[0], kc[5],kc[5], kh[0], kh[0]],1);															//Cyborg solo introduction
kp[10] = Pseq([ kh[1], kh[1], kc[5],kc[5], kh[1], kh[1]],1);														//Robot solo introduction
kp[11] = Pseq([ kp[1].value(3), kp[3] ]);																				//Improvised 7776 riff

//Kanjira Solos
ks = {arg length; Psync(Pxrand(ki, inf), length, length)};								//Improvisation using cells (CyborgSolo)
ks2 = {arg length; Psync(Prand([r.value(0.5), r.value(0.25), kp[0], kp[1].value(1), kp[2], kp[3]],inf), length, length)}; //Improvisation using patterns (humanSolo)

//Guitar buffers
gb[0]= Buffer.read(s,"sounds/gSamples/guitarMain.aif");
gb[1]	= Buffer.read(s,"sounds/gSamples/guitarMainDP.aif");
gb[2] = Buffer.read(s,"sounds/gSamples/guitarMainBuildPM.aif");
gb[3] = Buffer.read(s,"sounds/gSamples/guitarMainBuild.aif");
gb[4] = Buffer.read(s,"sounds/gSamples/guitar7fill.aif");
gb[5] = Buffer.read(s,"sounds/gSamples/guitar7fill2.aif");
gb[6] = Buffer.read(s,"sounds/gSamples/guitar7fill3.aif");
gb[7] = Buffer.read(s,"sounds/gSamples/guitar5fill.aif");
gb[8] = Buffer.read(s,"sounds/gSamples/guitar6fill.aif");
gb[9] = Buffer.read(s,"sounds/gSamples/guitar6fill2.aif");
gb[10] = Buffer.read(s,"sounds/gSamples/guitar6fill3.aif");
gb[11] = Buffer.read(s,"sounds/gSamples/guitar7776.aif");
gb[12] = Buffer.read(s,"sounds/gSamples/guitar77762.aif");
gb[13] = Buffer.read(s,"sounds/gSamples/guitar77763.aif");

s.sync;		//Necessary to be able to get number of frames assigned for each buffer

//Guitar Functions
	
	//Standard function to play a duration of a guitar phrase
gf = { arg start = 0, xdur=0.5, xamp=1, xbufnum; Pbind([\instrument, \startPos, \dur, \amp, \bufnum, \pan], Pseq([[\guitar, start, xdur, xamp, xbufnum, 0.5]],1) )};

	//Guitar improvisation function for recorded phrases of 7/16 length
gif7 = { arg length, xdur=[0.25,0.5], xpan=(0.02,0.3..0.7); Psync(Pbind(	\instrument, \guitar,
															\startPos, Prand((0..6)*(gb[0].numFrames/7),inf),	//Random 
															\dur, Prand(xdur,inf), 
															\amp, Prand((0.7,0.8..1),inf), 
															\bufnum, Prand(gb.copySeries(0,1,6),inf), 
															\pan, Prand(xpan,inf)
													),length, length)};
													
	//Guitar improvisation function for recorded phrases of 6/16 length											
gif6 = { arg length, xdur=[0.25,0.5], xpan=(0.02,0.3..0.7); Psync(Pbind(	\instrument, \guitar, 
															\startPos, Prand((0..5)*(gb[0].numFrames/6),inf), 
															\dur, Prand(xdur,inf), \amp, Prand((0.7,0.8..1),inf), 
															\bufnum, Prand(gb.copySeries(7,8,10),inf),
															\pan, Prand(xpan,inf)
											), length, length)};

//Guitar Recordings and cells
gc[0] = gf.value(0, 1.75, 1, gb[0].bufnum);																	// Main
gc[1] = gf.value(0, 1.75, 1, gb[1].bufnum);																	// Main double picked
gc[2] = gf.value(0, 1.75, 1, gb[2].bufnum);																	// Main Build Palm Muted
gc[3] = gf.value(0, 1.75, 1, gb[3].bufnum);																	// Main Build
gc[4] = gf.value(0, 1.75, 1, gb[4].bufnum);																	// 7 Fill
gc[5] = gf.value(0, 1.75, 1, gb[5].bufnum);																	// 7 Fill 2
gc[6] = gf.value(0, 1.75, 1, gb[6].bufnum);																	// 7 Fill 3
gc[7] = gf.value(0, 1.25, 1, gb[7].bufnum);																	// 5 Fill
gc[8] = gf.value(0, 1.5, 1, gb[8].bufnum);																	// 6 Fill
gc[9] = gf.value(0, 1.5, 1, gb[9].bufnum);																	// 6 Fill 2
gc[10] = gf.value(0, 1.5, 1, gb[10].bufnum);																	// 6 Fill 3
gc[11] = gf.value((gb[11].numFrames/27)*21, 1.5, 1, gb[11].bufnum);										// 6 Fill 4
gc[12] = gf.value((gb[12].numFrames/27)*21, 1.5, 1, gb[12].bufnum);										// 6 Fill 5
gc[13] = gf.value((gb[13].numFrames/27)*21, 1.5, 1, gb[13].bufnum);										// 6 Fill 6
gc[14] = {arg dur; gf.value((gb[12].numFrames/27)*22, dur, 1, gb[12].bufnum)};							// High slide	
gc[15] = {arg dur; gf.value((gb[12].numFrames/27)*21, dur, 1, gb[12].bufnum)};							// High 5th
gc[16] = {arg dur; gf.value(0, dur, 1, gb[0].bufnum)};														// Root Note
gc[17] = Pseq([ gc[16].value(0.5), r.value(1),r.value(0.25)],1);											// 7 Accomp
gc[18] = Pseq([ gc[16].value(0.5), r.value(0.25)], 2);														// 6 Accomp
gc[19] = gf.value(0, 0.5, 0.5, gb[2].bufnum);																	// Quiet Palm mute root, 2 hits
gc[20] = gf.value(0, 0.25, 0.5, gb[2].bufnum);																// Quiet Palm mute root, 1 hit
gc[21] = {arg dur; Pseq([gc[19], r.value(dur-0.5)],1)};														// Quiet Palm mute root 2 hits, with rests 
gc[22] = {arg dur; Pseq([gc[20],r.value(dur-0.25)], 1)};													// Quiet Palm mute root 1 hit, with rests 

//Guitar Patterns
gp[0] = Pseq([gc[0]],3);																							// 777 Basic
gp[1] = gf.value(0, 5.25, 1, gb[11].bufnum);																	// 777 2
gp[2] = gf.value(0, 5.25, 1, gb[12].bufnum);																	// 777 3		
gp[3] = gf.value(0, 5.25, 1, gb[13].bufnum);																	// 777 4
gp[4] = {arg reps; Prand([gc[0], gc[1], gc[2], gc[3]], reps)};												// 777 Randoms
gp[5] = {arg dur1, dur2; Pseq([ gc[16].value(dur2), gc[16].value(dur2), Pseq([gc[14].value(dur1)],4), Pseq([gc[15].value(dur1)],4), gc[16].value(dur2), gc[16].value(dur2)],1)};	// Solo intro/outro
gp[6] = {arg length; Psync(Pseq([gp[7].value(5.25), gp[7].value(0.75), gp[7].value(0.75)],inf), length, length)};																			// 7776 Accomp pattern
gp[7] = {arg length; Psync(Prand([gc[21].value(1.75),gc[22].value(1.75)], inf), length, length)};																								// 1/8 accomp pattern
gp[8] = Pxrand(gc.copySeries(4,5,13).add(gc[0]), 1);																																			//Human Improvisation pattern 

//Guitar Solos
gs1 = {arg length; Psync(Prand([gp[8], Pseq([r.value(0.5), gp[8]])]), length, length)};
gs2 = {arg length, xpan; Psync(Pwrand([gif7.value(1, (0.0625..0.125), xpan), gif6.value(1, [0.125, 0.0625], xpan)], [1,2].normalizeSum,inf), length, length)};

//Sections
intro = Pseq([r.value((4..8).choose), ks.value([10,20].choose), kp[4], ks.value([10,20].choose), Pseq([kh[0]],3)]);

mainTheme = Ppar([	Pseq([kp[4]],2),
							Pseq([gp[0], gc[8], gp[0], gc[9]	])	
							]);

improvTheme = Ppar ([		Pseq([gp[4].value(3), gc[9]]),
								kp[11]
								],2);
						
altTheme = Ppar ([	Pseq([ gp[2], gc[12], gp[2], Prand([gc[8], gc[11], gc[13]],1)]),
						Pseq([kp[11]],2)
						],1);

preSolo = Pseq([	Ppar([		gp[1], 
								kp[8].value( 1.75, 5.25)
								]),
					ks.value(1.5)
					],2);
					
if(0.3.coin, {hSoloRep=2;"Greedy Human!".postln}, {hSoloRep=1}); 	//Because sometimes Humans are greedy
if(0.2.coin, {hSoloRep=2;"Greedy Cyborg!".postln}, {hSoloRep=1}); 	//Because Cyborgs have greed emulation.
																			//Robots are never greedy
																			
humanSolo = 				Pseq([			Ppar([kp[5], gp[5].value(0.25, 0.5)]), 		
												ks2.value(13.5),		
											Ppar([kp[5], gp[5].value(0.25, 0.5)]),
												Ppar([ Pseq([Pseq([gs1.value(1.75)],3), gs1.value(1.5)],2), 
														Pseq([kp[8].value(1.75, 5.25), Pseq([kp[8].value(0.75,0.75)],2)],2)]), 			
											Ppar([kp[5], gp[5].value(0.25, 0.5)])			
											],hSoloRep);

cyborgSolo = 				Pseq([		Ppar([	kp[9], gp[5].value(0.125,0.5)]),								
												ks.value(x=[13.5,27].choose),
										Ppar([	kp[9], gp[5].value(0.125,0.5)]),
												Ppar([ Pseq([kp[8].value(1.75, 5.25), Pseq([kp[8].value(0.75,0.75)],2)], (x/6.75)), 
														Psync(Prand([ gif7.value(1, [0.5, 0.25]), 	gif6.value(1, [0.5, 0.25])		],inf), x,x)]),
										Ppar([		kp[9], gp[5].value(0.125,0.5)]) 			
										],1);

robotSolo = 				Pseq([		Ppar([kp[10], gp[5].value(0.125,0.25)]), 		
											Pseq([	kif.value(6.75, (0.0625..0.125)), 
													Ppar([	gs2.value(6.75, (-1.0,-0.9..1.0)), 
														Pseq([kp[8].value(1.75, 5.25), Pseq([kp[8].value(0.75,0.75)],2)],1 ) ])], [3,5].choose), 
										Ppar([kp[10], gp[5].value(0.125,0.25)])
										],1);			

ending = Pseq([Pseq([kh[1], kc[4], kc[5]], 2), Ppar([kp[1].value(3),gp[0]]), Ppar([kh[0], gc[16].value(0.5)],3)],1);

						
//Playback													
Ppar([		dronePat,
			Pseq([		intro, 
						mainTheme, 
						improvTheme, 
						altTheme,
						mainTheme, 
						preSolo, 
						humanSolo, r.value(1), 
						cyborgSolo, r.value(1),
						robotSolo, r.value(1), 																
						mainTheme, 
						ending
						],1)
		]).play(TempoClock(100/60));			//Playback at 100bpm

}.fork

)
Buffer.freeAll 		//Useful when all buffers get used up during testing






