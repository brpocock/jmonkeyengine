__version__ = '$Revision$'
__date__ = '$Date$'
__author__ = 'Blaine Simpson, blaine (dot) simpson (at) admc (dot) com'
__url__ = 'http://www.jmonkeyengine.com'

# Copyright (c) 2009, Blaine Simpson and the jMonkeyEngine team
# All rights reserved.
#
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions are met:
#     * Redistributions of source code must retain the above copyright
#       notice, this list of conditions and the following disclaimer.
#     * Redistributions in binary form must reproduce the above copyright
#       notice, this list of conditions and the following disclaimer in the
#       documentation and/or other materials provided with the distribution.
#     * Neither the name of the <organization> nor the
#       names of its contributors may be used to endorse or promote products
#       derived from this software without specific prior written permission.
#
# THIS SOFTWARE IS PROVIDED BY Blaine Simpson and the jMonkeyEngine team
# ''AS IS'' AND ANY
# EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
# WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
# DISCLAIMED. IN NO EVENT SHALL Blaine Simpson or the jMonkeyEngine team
# BE LIABLE FOR ANY
# DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
# (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
# LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
# ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
# (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
# SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

from bpy.data import scenes as _bScenes

class ActionData(object):
    """
    This class encapsulates preparation of the data of a single Blender Action
    applied to a single Armature.

    Usage:  instantiate, addPose for all 'blenderFrames', then cull().
    The locs and mats dictionaries are then good to use.

    The key sets for locs, rots, mats is always the same FOR NOW.
    """
    __slots__ = (
            'blenderFrames', 'keyframeTimes', 'locs', 'rots', 'mats', 'name')
    # Not sure which of rots and/or mats will be used.
    # For now, using 'rots' just to cull <rotations> with no rotations.
    frameRate = None

    def getChannelNames(self):
        return set(self.locs.keys() + self.rots.keys() + self.mats.keys())

    def getName(self):
        return self.name

    def __init__(self, bAction):
        object.__init__(self)
        self.name = bAction.name
        if ActionData.frameRate == None:
            raise Exception("You can't instantiate ActionData until the "
                    + "frameRate is updated")

        # Initialize candidate loc and rot lists
        self.locs = {}
        self.rots = {}
        self.mats = {}
        for boneName in bAction.getChannelNames():
            self.locs[boneName] = []
            self.rots[boneName] = []
            self.mats[boneName] = []

        # Purpose of this block is to get a set of the SIGNIFICANT frames,
        # 'blenderFrames'
        frameSet = set()
        for ipos in bAction.getAllChannelIpos().itervalues():
            if ipos == None or len(ipos) < 1: continue
            for curve in ipos:
                # There are normally 7 curves for an IPO.
                # However, if there is a Quat IPO, all 4 Quat* will be present,
                # and if there is a Loc IP, all 3 Loc* will be present.
                # Therefore, just test one of each.
                if curve.name not in ['LocX', 'QuatW']: continue
                for bp in curve.bezierPoints:
                    frameFloat = bp.vec[1][0]
                    # Remember that Blender frames are 1-based, not 0-based
                    if frameFloat % 1 != 0: raise Exception(
                            "A curve Bezier pt has non-integral frame num: "
                            + str(frameFloat))
                    frameSet.add(frameFloat)
                    # Leaving value a float so float division will occur below
        self.blenderFrames = list(frameSet)
        self.blenderFrames.sort()
        self.keyframeTimes = []
        for frameNum in self.blenderFrames:
            self.keyframeTimes.append((frameNum-1.) / ActionData.frameRate)

    def addPose(self, poseBones):
        for boneName in self.locs.iterkeys():  # equivalent to self.rots...
            locs = self.locs[boneName]
            rots = self.rots[boneName]
            mats = self.mats[boneName]
            if boneName in poseBones.keys():
                locs.append(
                        poseBones[boneName].poseMatrix.translationPart())
                rots.append(poseBones[boneName]
                        .poseMatrix.rotationPart().toQuat())
                mats.append(poseBones[boneName].poseMatrix.copy())
            else:
                raise Exception(
            "TODO:  Figure out what to do when channel has no val for a frame");
                locs.append(None)
                rots.append(None)
                mats.append(None)

    def cull(self):
        usedRotChannels = set()
        usedLocChannels = set()
        for boneName, oneBoneRots in self.rots.iteritems():
            for quat in oneBoneRots:
                if (round(quat.x, 6) != 0 or round(quat.y, 6) != 0
                        or round(quat.z, 6) != 0 or round(quat.w) != 1):
                    usedRotChannels.add(boneName)
        for boneName, oneBoneLocs in self.locs.iteritems():
            for loc in oneBoneLocs:
                if (round(loc[0], 6) != 0. or round(loc[1], 6) != 0.
                        or round(loc[2], 6) != 0.):
                    usedLocChannels.add(boneName)

        # It is likely that we can remove items from just the loc or the rot
        # list, but until we get far enough along to test this, we must be
        # safe and assume that a non-zero raw loc could produced derived
        # non-zero rot; and that a non-zero raw rot could produce derived
        # non-zero loc.
        for zapBone in (
            set(self.boneRots.keys()) - (usedRotChannels & usedLocChannels)):
                del self.boneRots[zapbone]
                del self.boneLocs[zapbone]
                del self.boneMats[zapbone]
        if len(self.boneRots + self.boneLocs + self.boneMast) < 1:
            print "No significant channels"
            self.blenderFrames = None
        return   # READ COMMENT ABOVE about the more liberal culling below.

        for rotZapBone in set(self.boneRots.keys()) - usedRotChannels:
            print "Zapping bone '" + rotZapBone + "' from rotations"
            del self.rots[rotZapBone]
        for locZapBone in set(self.boneLocs.keys()) - usedLocChannels:
            print "Zapping bone '" + locZapBone + "' from locations"
            del self.locs[locZapBone]
            if locZapBone not in set(self.bonRots.keys()):
                del self.mats[locZapBone]

    def updateFrameRate():
        ActionData.frameRate =  \
                _bScenes.active.getRenderingContext().framesPerSec()

    updateFrameRate = staticmethod(updateFrameRate)
