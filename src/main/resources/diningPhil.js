var PHILOSOPHER_COUNT = 5;
var pickProgram;
function addPhil(philNum) {
	var j = (philNum % PHILOSOPHER_COUNT) + 1;
	bp.registerBThread("Phil" + philNum, function () {
		while (true) {
			if(philNum%2 == 0)
			{
				bp.sync({ request: bp.Event("Philosopher "+philNum+": Pick stick " + philNum) });
				bp.sync({ request: bp.Event("Philosopher "+philNum+": Pick stick " + j) });
				bp.sync({ request: bp.Event("Philosopher "+philNum+": Eating") });
				bp.sync({ request: bp.Event("Philosopher "+philNum+": Rel stick " + philNum) });
				bp.sync({ request: bp.Event("Philosopher "+philNum+": Rel stick " + j) });
				bp.sync({ request: bp.Event("Philosopher "+philNum+": Thinking") });
			}
			else
			{
				bp.sync({ request: bp.Event("Philosopher "+philNum+": Pick stick " + j) });
				bp.sync({ request: bp.Event("Philosopher "+philNum+": Pick stick " + philNum) });
				bp.sync({ request: bp.Event("Philosopher "+philNum+": Eating") });
				bp.sync({ request: bp.Event("Philosopher "+philNum+": Rel stick " +j) });
				bp.sync({ request: bp.Event("Philosopher "+philNum+": Rel stick " + philNum) });
				bp.sync({ request: bp.Event("Philosopher "+philNum+": Thinking") });
				
			}
		}
	});	
}


function addPhili(philNum) {
	var j = (philNum % PHILOSOPHER_COUNT) + 1;
	bp.registerBThread("Phil" + philNum, function () {
		while (true) {
			bp.sync({ request: bp.Event("Philosopher "+philNum+": Pick stick " + philNum)});
			bp.sync({ request: bp.Event("Philosopher "+philNum+": Pick stick " + j) });
			bp.sync({ request: bp.Event("Philosopher "+philNum+": Eating") });
			bp.sync({ request: bp.Event("Philosopher "+philNum+": Rel stick " + j) });
			bp.sync({ request: bp.Event("Philosopher "+philNum+": Rel stick " +philNum) });
			bp.sync({ request: bp.Event("Philosopher "+philNum+": Thinking") });
			
		}
	});
}


function addStick(i) {
	var j = (i-1 % PHILOSOPHER_COUNT);
	if(i == 1){
		j=5;
	}
	
	bp.registerBThread("Stick" + i, function () {
		var pickMe1 =   ("Philosopher "+ i +": Pick stick " + i);
		var pickMe2 =   ("Philosopher "+ j +": Pick stick " + i);
		var releaseMe1 = ("Philosopher "+ i +": Rel stick " + i);
		var releaseMe2 = ("Philosopher "+ j +": Rel stick " + i);		

		while (true) {
			var e = bp.sync({waitFor: [bp.Event(pickMe1),bp.Event(pickMe2)]});
			var wt = (e.name == pickMe1)? releaseMe1: releaseMe2;
			var cond= bp.sync({waitFor: bp.Event(wt),
							   block: [bp.Event(pickMe1),bp.Event(pickMe2)]});
		}
	});
}

for (var i = 1; i <= PHILOSOPHER_COUNT; i++) {
	addStick(i);
	if (pickProgram == 0) {
		addPhil(i);
	} else {
		addPhili(i);
	}
}